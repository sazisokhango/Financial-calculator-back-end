package com.psybergate.financialcalculator.bond;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psybergate.financialcalculator.dto.PropertyBondRequest;
import com.psybergate.financialcalculator.dto.RegisterRequest;
import com.psybergate.financialcalculator.repository.InvestmentForecastRepository;
import com.psybergate.financialcalculator.repository.PropertyBondRepository;
import com.psybergate.financialcalculator.repository.TaxCalculationRepository;
import com.psybergate.financialcalculator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PropertyBondSpec {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PropertyBondRepository bondRepository;
    @Autowired private InvestmentForecastRepository forecastRepository;
    @Autowired private TaxCalculationRepository taxCalculationRepository;
    @Autowired private UserRepository userRepository;

    private String userEmail;
    private String userBEmail;

    @BeforeEach
    void setUp() throws Exception {
        bondRepository.deleteAll();
        forecastRepository.deleteAll();
        taxCalculationRepository.deleteAll();
        userRepository.deleteAll();
        userEmail = "saziso@example.com";
        userBEmail = "john@example.com";
        registerUser("Saziso", "Khango", userEmail);
        registerUser("John", "Doe", userBEmail);
    }

    // ── US1: Create and Save a Bond Plan ─────────────────────────────────────

    @Test
    void givenValidInputs_whenCreate_thenReturns201WithCorrectProjection() throws Exception {
        PropertyBondRequest req = buildRequest(userEmail, "Family Home Bond", "Primary residence",
                bd("1200000"), bd("12000"), 2, bd("11.00"));

        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userEmail").value(userEmail))
                .andExpect(jsonPath("$.title").value("Family Home Bond"))
                .andExpect(jsonPath("$.forecastResults.totalLoanAmount").value(1200000.00))
                .andExpect(jsonPath("$.monthlyProjection.length()").value(2))
                .andExpect(jsonPath("$.monthlyProjection[0].interestCharged").value(11000.00))
                .andExpect(jsonPath("$.monthlyProjection[0].principalPaid").value(1000.00))
                .andExpect(jsonPath("$.monthlyProjection[0].endingBalance").value(1199000.00))
                .andExpect(jsonPath("$.monthlyProjection[1].interestCharged").value(10990.83))
                .andExpect(jsonPath("$.monthlyProjection[1].startingBalance").value(1199000.00));
    }

    @Test
    void givenBlankTitle_whenCreate_thenReturns400() throws Exception {
        PropertyBondRequest req = buildRequest(userEmail, "", null,
                bd("1200000"), bd("12000"), 12, bd("11.00"));

        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void givenTermMonthsZero_whenCreate_thenReturns400() throws Exception {
        PropertyBondRequest req = buildRequest(userEmail, "Bond", null,
                bd("1200000"), bd("12000"), 0, bd("11.00"));

        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInterestRateAbove100_whenCreate_thenReturns400() throws Exception {
        PropertyBondRequest req = buildRequest(userEmail, "Bond", null,
                bd("1200000"), bd("12000"), 12, bd("101.00"));

        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNegativeInitialAmount_whenCreate_thenReturns400() throws Exception {
        PropertyBondRequest req = buildRequest(userEmail, "Bond", null,
                bd("-1"), bd("12000"), 12, bd("11.00"));

        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenUnregisteredEmail_whenCreate_thenReturns404() throws Exception {
        PropertyBondRequest req = buildRequest("notexisting@example.com", "Bond", null,
                bd("1200000"), bd("12000"), 12, bd("11.00"));

        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void givenZeroInterestRate_whenCreate_thenReturns201WithNoInterest() throws Exception {
        PropertyBondRequest req = buildRequest(userEmail, "Zero Rate Bond", null,
                bd("12000"), bd("6000"), 2, bd("0.00"));

        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.monthlyProjection[0].interestCharged").value(0.00))
                .andExpect(jsonPath("$.monthlyProjection[0].principalPaid").value(6000.00))
                .andExpect(jsonPath("$.forecastResults.fullyPaid").value(true));
    }

    // ── US2: View All Bond Plans for a User ───────────────────────────────────

    @Test
    void givenTwoBondsForUserA_whenGetAll_thenReturns200WithBothRecords() throws Exception {
        createBond(userEmail, "Bond 1");
        createBond(userEmail, "Bond 2");

        mockMvc.perform(get("/api/bonds").param("userEmail", userEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenNoBondsForUser_whenGetAll_thenReturns200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/bonds").param("userEmail", userEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenBondsForBothUsers_whenGetAllForUserA_thenOnlyUserABondsReturned() throws Exception {
        createBond(userEmail, "A Bond");
        createBond(userBEmail, "B Bond");

        mockMvc.perform(get("/api/bonds").param("userEmail", userEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value(userEmail));
    }

    @Test
    void givenUnregisteredEmail_whenGetAll_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/bonds").param("userEmail", "notexisting@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ── US3: View a Single Bond Plan ──────────────────────────────────────────

    @Test
    void givenExistingBond_whenGetById_thenReturns200WithFullRecord() throws Exception {
        Long id = createBondAndGetId(userEmail, "My Home Bond");

        mockMvc.perform(get("/api/bonds/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("My Home Bond"))
                .andExpect(jsonPath("$.forecastResults.totalLoanAmount").isNumber())
                .andExpect(jsonPath("$.monthlyProjection.length()").value(12));
    }

    @Test
    void givenNonExistentId_whenGetById_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/bonds/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Property bond not found"));
    }

    // ── US4: Update a Bond Plan ───────────────────────────────────────────────

    @Test
    void givenExistingBond_whenUpdateWithNewTermMonths_thenReturns200WithRecalculation() throws Exception {
        Long id = createBondAndGetId(userEmail, "Bond to Update");

        PropertyBondRequest update = buildRequest(userEmail, "Bond to Update", null,
                bd("1200000"), bd("12000"), 24, bd("11.00"));

        mockMvc.perform(put("/api/bonds/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.monthlyProjection.length()").value(24));
    }

    @Test
    void givenUpdateWithInvalidTermMonths_whenPut_thenReturns400() throws Exception {
        Long id = createBondAndGetId(userEmail, "Bond");

        PropertyBondRequest bad = buildRequest(userEmail, "Bond", null,
                bd("1200000"), bd("12000"), 0, bd("11.00"));

        mockMvc.perform(put("/api/bonds/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentId_whenPut_thenReturns404() throws Exception {
        PropertyBondRequest req = buildRequest(userEmail, "Bond", null,
                bd("1200000"), bd("12000"), 12, bd("11.00"));

        mockMvc.perform(put("/api/bonds/999999").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Property bond not found"));
    }

    @Test
    void givenChangedMonthlyContribution_whenUpdate_thenForecastResultsChange() throws Exception {
        Long id = createBondAndGetId(userEmail, "Bond");

        PropertyBondRequest update = buildRequest(userEmail, "Bond", null,
                bd("1200000"), bd("20000"), 12, bd("11.00"));

        mockMvc.perform(put("/api/bonds/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.forecastResults.totalRepayments").isNumber());
    }

    // ── US5: Delete a Bond Plan ───────────────────────────────────────────────

    @Test
    void givenExistingBond_whenDelete_thenReturns204() throws Exception {
        Long id = createBondAndGetId(userEmail, "Bond to Delete");

        mockMvc.perform(delete("/api/bonds/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenDeletedBond_whenGetById_thenReturns404() throws Exception {
        Long id = createBondAndGetId(userEmail, "Gone Bond");

        mockMvc.perform(delete("/api/bonds/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bonds/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Property bond not found"));
    }

    @Test
    void givenNonExistentId_whenDelete_thenReturns404() throws Exception {
        mockMvc.perform(delete("/api/bonds/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Property bond not found"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void registerUser(String first, String last, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(first, last, email))))
                .andExpect(status().isCreated());
    }

    private void createBond(String email, String title) throws Exception {
        mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildRequest(email, title, null, bd("1200000"), bd("12000"), 12, bd("11.00")))));
    }

    private Long createBondAndGetId(String email, String title) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/bonds").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRequest(email, title, null, bd("1200000"), bd("12000"), 12, bd("11.00")))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private PropertyBondRequest buildRequest(String email, String title, String description,
                                             BigDecimal initial, BigDecimal monthly,
                                             int termMonths, BigDecimal rate) {
        PropertyBondRequest r = new PropertyBondRequest();
        r.setUserEmail(email);
        r.setTitle(title);
        r.setDescription(description);
        r.setInitialAmount(initial);
        r.setMonthlyContribution(monthly);
        r.setTermMonths(termMonths);
        r.setInterestRate(rate);
        return r;
    }

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }
}
