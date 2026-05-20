package com.psybergate.financialcalculator.tax;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psybergate.financialcalculator.dto.RegisterRequest;
import com.psybergate.financialcalculator.dto.TaxCalculationRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaxCalculationSpec {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private TaxCalculationRepository taxCalculationRepository;

    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        taxCalculationRepository.deleteAll();
        userRepository.deleteAll();
        userId = registerAndGetId("Saziso", "Khango", "saziso@example.com");
    }

    // ── US1: Salaried employee under 65 ──────────────────────────────────────

    @Test
    void givenSalary500k_age35_whenCalculate_thenReturns201WithCorrectBreakdown() throws Exception {
        TaxCalculationRequest req = request(userId, "My 2025 Tax", null,
                bd("500000"), null, null, null, null, null, 35, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.title").value("My 2025 Tax"))
                .andExpect(jsonPath("$.totalIncome").value(500000.00))
                .andExpect(jsonPath("$.totalDeductions").value(0.00))
                .andExpect(jsonPath("$.netTaxableIncome").value(500000.00))
                .andExpect(jsonPath("$.taxBeforeRebate").value(117507.00))
                .andExpect(jsonPath("$.rebate").value(17235.00))
                .andExpect(jsonPath("$.taxAlreadyPaid").value(0.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(100272.00));
    }

    @Test
    void givenAllFieldsZero_age25_whenCalculate_thenFinalLiabilityIsZero() throws Exception {
        TaxCalculationRequest req = request(userId, "Zero Income", null,
                BigDecimal.ZERO, null, null, null, null, null, 25, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalIncome").value(0.00))
                .andExpect(jsonPath("$.taxBeforeRebate").value(0.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(0.00));
    }

    // ── US2: Age-based rebate tiers ───────────────────────────────────────────

    @Test
    void givenSalary500k_age70_whenCalculate_thenSecondaryRebateApplied() throws Exception {
        TaxCalculationRequest req = request(userId, "Senior Tax 65", null,
                bd("500000"), null, null, null, null, null, 70, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rebate").value(26679.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(90828.00));
    }

    @Test
    void givenSalary500k_age75_whenCalculate_thenTertiaryRebateApplied() throws Exception {
        TaxCalculationRequest req = request(userId, "Senior Tax 75", null,
                bd("500000"), null, null, null, null, null, 75, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rebate").value(29824.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(87683.00));
    }

    @Test
    void givenAge64_whenCalculate_thenOnlyPrimaryRebate() throws Exception {
        TaxCalculationRequest req = request(userId, "Under 65", null,
                bd("500000"), null, null, null, null, null, 64, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rebate").value(17235.00));
    }

    // ── US3: Multiple income sources and deductions ───────────────────────────

    @Test
    void givenMixedIncome_whenCalculate_thenTotalIncomeAndDeductionsCorrect() throws Exception {
        TaxCalculationRequest req = request(userId, "Mixed Income", null,
                bd("400000"), bd("10000"), null, null, bd("50000"), bd("24000"), 40, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalIncome").value(460000.00))
                .andExpect(jsonPath("$.totalDeductions").value(24000.00))
                .andExpect(jsonPath("$.netTaxableIncome").value(436000.00))
                .andExpect(jsonPath("$.taxBeforeRebate").value(97667.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(80432.00));
    }

    @Test
    void givenRetirementAnnuityExceedsTotalIncome_whenCalculate_thenNetTaxableIncomeIsZero() throws Exception {
        TaxCalculationRequest req = request(userId, "RA > Income", null,
                bd("5000"), null, null, null, null, bd("10000"), 35, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.netTaxableIncome").value(0.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(0.00));
    }

    @Test
    void givenTaxAlreadyPaidExceedsTaxOwed_whenCalculate_thenFinalLiabilityIsZero() throws Exception {
        TaxCalculationRequest req = request(userId, "Overpaid", null,
                bd("500000"), null, null, null, null, null, 35, bd("200000"));

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.finalTaxLiability").value(0.00));
    }

    // ── US4: Reject invalid or missing inputs ─────────────────────────────────

    @Test
    void givenMissingTitle_whenCalculate_thenReturns400() throws Exception {
        TaxCalculationRequest req = request(userId, null, null,
                bd("500000"), null, null, null, null, null, 35, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void givenNegativeSalary_whenCalculate_thenReturns400() throws Exception {
        TaxCalculationRequest req = request(userId, "Negative", null,
                bd("-1000"), null, null, null, null, null, 35, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNegativeRetirementAnnuity_whenCalculate_thenReturns400() throws Exception {
        TaxCalculationRequest req = request(userId, "Negative RA", null,
                bd("500000"), null, null, null, null, bd("-1"), 35, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentUserId_whenCalculate_thenReturns404() throws Exception {
        TaxCalculationRequest req = request(999999L, "Test", null,
                bd("500000"), null, null, null, null, null, 35, null);

        mockMvc.perform(post("/api/tax").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long registerAndGetId(String first, String last, String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(first, last, email))))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    private TaxCalculationRequest request(Long userId, String title, String description,
                                          BigDecimal salary, BigDecimal interest, BigDecimal dividend,
                                          BigDecimal capitalGain, BigDecimal bonus,
                                          BigDecimal ra, Integer age, BigDecimal taxPaid) {
        TaxCalculationRequest r = new TaxCalculationRequest();
        r.setUserId(userId);
        r.setTitle(title);
        r.setDescription(description);
        r.setSalary(salary);
        r.setInterestIncome(interest);
        r.setDividend(dividend);
        r.setCapitalGain(capitalGain);
        r.setBonus(bonus);
        r.setRetirementAnnuity(ra);
        r.setAge(age);
        r.setTaxAlreadyPaid(taxPaid);
        return r;
    }
}
