package com.psybergate.financialcalculator.investment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psybergate.financialcalculator.dto.InvestmentForecastRequest;
import com.psybergate.financialcalculator.dto.RegisterRequest;
import com.psybergate.financialcalculator.repository.InvestmentForecastRepository;
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
class InvestmentForecastSpec {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private InvestmentForecastRepository forecastRepository;
    @Autowired private UserRepository userRepository;

    private Long userAId;
    private Long userBId;

    @BeforeEach
    void setUp() throws Exception {
        forecastRepository.deleteAll();
        userRepository.deleteAll();
        userAId = registerAndGetId("Saziso", "Khango", "saziso@example.com");
        userBId = registerAndGetId("John", "Doe", "john@example.com");
    }

    // ── US1: Create an Investment Forecast ───────────────────────────────────

    @Test
    void givenValidInputs_whenCreate_thenReturns201WithCorrectMonthlyProjection() throws Exception {
        InvestmentForecastRequest req = buildRequest(userAId, "Retirement Plan",
                new BigDecimal("10000.00"), new BigDecimal("2000.00"), 2, new BigDecimal("12.00"));

        mockMvc.perform(post("/api/investments/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(userAId))
                .andExpect(jsonPath("$.monthlyProjection.length()").value(2))
                .andExpect(jsonPath("$.monthlyProjection[0].interestEarned").value(100.00))
                .andExpect(jsonPath("$.monthlyProjection[0].endingBalance").value(12100.00))
                .andExpect(jsonPath("$.monthlyProjection[1].interestEarned").value(121.00))
                .andExpect(jsonPath("$.monthlyProjection[1].endingBalance").value(14221.00));
    }

    @Test
    void givenBlankTitle_whenCreate_thenReturns400() throws Exception {
        InvestmentForecastRequest req = buildRequest(userAId, "  ",
                new BigDecimal("10000.00"), new BigDecimal("2000.00"), 12, new BigDecimal("10.00"));

        mockMvc.perform(post("/api/investments/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenTermMonthsZero_whenCreate_thenReturns400() throws Exception {
        InvestmentForecastRequest req = buildRequest(userAId, "Test",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), 0, new BigDecimal("10.00"));

        mockMvc.perform(post("/api/investments/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAnnualRateOver100_whenCreate_thenReturns400() throws Exception {
        InvestmentForecastRequest req = buildRequest(userAId, "Test",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), 12, new BigDecimal("101.00"));

        mockMvc.perform(post("/api/investments/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentUserId_whenCreate_thenReturns404() throws Exception {
        InvestmentForecastRequest req = buildRequest(999999L, "Test",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), 12, new BigDecimal("10.00"));

        mockMvc.perform(post("/api/investments/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void givenZeroInterestRate_whenCreate_thenReturns201WithZeroInterest() throws Exception {
        InvestmentForecastRequest req = buildRequest(userAId, "Zero Interest",
                new BigDecimal("5000.00"), new BigDecimal("1000.00"), 3, new BigDecimal("0.00"));

        mockMvc.perform(post("/api/investments/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.monthlyProjection[0].interestEarned").value(0.00))
                .andExpect(jsonPath("$.monthlyProjection[1].interestEarned").value(0.00));
    }

    // ── US2: View All Saved Forecasts ────────────────────────────────────────

    @Test
    void givenTwoForecastsForUserA_whenGetAll_thenReturns200WithBothRecords() throws Exception {
        createForecast(userAId, "Plan A1");
        createForecast(userAId, "Plan A2");

        mockMvc.perform(get("/api/investments").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenNoForecasts_whenGetAll_thenReturns200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/investments").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenForecastsForBothUsers_whenGetAllForUserA_thenOnlyUserARecordsReturned() throws Exception {
        createForecast(userAId, "A Plan");
        createForecast(userBId, "B Plan");

        mockMvc.perform(get("/api/investments").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userAId));
    }

    @Test
    void givenNonExistentUserId_whenGetAll_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/investments").param("userId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ── US3: View a Single Forecast ──────────────────────────────────────────

    @Test
    void givenExistingForecast_whenGetById_thenReturns200WithFullRecord() throws Exception {
        Long id = createForecastAndGetId(userAId, "My Plan");

        mockMvc.perform(get("/api/investments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("My Plan"))
                .andExpect(jsonPath("$.forecastResults.projectedValue").isNumber())
                .andExpect(jsonPath("$.monthlyProjection.length()").value(12));
    }

    @Test
    void givenNonExistentId_whenGetById_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/investments/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Investment forecast not found"));
    }

    // ── US4: Update a Saved Forecast ─────────────────────────────────────────

    @Test
    void givenExistingForecast_whenUpdateWithNewTermMonths_thenReturns200WithRecalculation() throws Exception {
        Long id = createForecastAndGetId(userAId, "Original");

        InvestmentForecastRequest update = buildRequest(userAId, "Updated",
                new BigDecimal("10000.00"), new BigDecimal("2000.00"), 24, new BigDecimal("12.00"));

        mockMvc.perform(put("/api/investments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.monthlyProjection.length()").value(24));
    }

    @Test
    void givenInvalidField_whenUpdate_thenReturns400() throws Exception {
        Long id = createForecastAndGetId(userAId, "Test");
        InvestmentForecastRequest bad = buildRequest(userAId, "Bad",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), 0, new BigDecimal("10.00"));

        mockMvc.perform(put("/api/investments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentId_whenUpdate_thenReturns404() throws Exception {
        InvestmentForecastRequest req = buildRequest(userAId, "Test",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), 12, new BigDecimal("10.00"));

        mockMvc.perform(put("/api/investments/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Investment forecast not found"));
    }

    @Test
    void givenDifferentUserId_whenUpdate_thenReturns400OwnershipMismatch() throws Exception {
        Long id = createForecastAndGetId(userAId, "UserA Forecast");
        InvestmentForecastRequest req = buildRequest(userBId, "Hijack",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), 12, new BigDecimal("10.00"));

        mockMvc.perform(put("/api/investments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User does not own this forecast"));
    }

    // ── US5: Delete a Forecast ───────────────────────────────────────────────

    @Test
    void givenExistingForecast_whenDelete_thenReturns204() throws Exception {
        Long id = createForecastAndGetId(userAId, "To Delete");

        mockMvc.perform(delete("/api/investments/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenDeletedForecast_whenGetById_thenReturns404() throws Exception {
        Long id = createForecastAndGetId(userAId, "Gone");

        mockMvc.perform(delete("/api/investments/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/investments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Investment forecast not found"));
    }

    @Test
    void givenNonExistentId_whenDelete_thenReturns404() throws Exception {
        mockMvc.perform(delete("/api/investments/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Investment forecast not found"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long registerAndGetId(String first, String last, String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(first, last, email))))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createForecast(Long userId, String title) throws Exception {
        mockMvc.perform(post("/api/investments/forecast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        buildRequest(userId, title,
                                new BigDecimal("10000.00"), new BigDecimal("1000.00"),
                                12, new BigDecimal("8.00")))));
    }

    private Long createForecastAndGetId(Long userId, String title) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/investments/forecast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRequest(userId, title,
                                        new BigDecimal("10000.00"), new BigDecimal("1000.00"),
                                        12, new BigDecimal("8.00")))))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private InvestmentForecastRequest buildRequest(Long userId, String title,
                                                    BigDecimal initial, BigDecimal monthly,
                                                    int termMonths, BigDecimal rate) {
        InvestmentForecastRequest r = new InvestmentForecastRequest();
        r.setUserId(userId);
        r.setTitle(title);
        r.setInitialAmount(initial);
        r.setMonthlyContribution(monthly);
        r.setTermMonths(termMonths);
        r.setAnnualInterestRate(rate);
        return r;
    }
}
