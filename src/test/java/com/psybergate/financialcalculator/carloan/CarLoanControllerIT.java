package com.psybergate.financialcalculator.carloan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psybergate.financialcalculator.dto.CarLoanRequest;
import com.psybergate.financialcalculator.repository.CarLoanRepository;
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
class CarLoanControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CarLoanRepository carLoanRepository;

    @BeforeEach
    void setUp() {
        carLoanRepository.deleteAll();
    }

    // ── (a) POST valid request → 201 with forecastResults and monthlyProjection ──

    @Test
    void givenValidRequest_whenCreate_thenReturns201WithForecastAndSchedule() throws Exception {
        CarLoanRequest req = buildRequest("BMW Finance Plan", "Monthly plan",
                bd("500000"), bd("80000"), bd("1500"), bd("69"),
                bd("0"), 60, bd("12"));

        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("BMW Finance Plan"))
                .andExpect(jsonPath("$.forecastResults.financedAmount").value(421500.00))
                .andExpect(jsonPath("$.forecastResults.fullyPaid").value(true))
                .andExpect(jsonPath("$.forecastResults.remainingBalance").value(0.00))
                .andExpect(jsonPath("$.forecastResults.estimatedPayoffMonth").isNumber())
                .andExpect(jsonPath("$.monthlyProjection.length()").value(60))
                .andExpect(jsonPath("$.monthlyProjection[0].month").value(1))
                .andExpect(jsonPath("$.monthlyProjection[0].startingBalance").value(421500.00))
                .andExpect(jsonPath("$.monthlyProjection[0].adminFee").value(69.00));
    }

    // ── (b) GET list → 200 array ──────────────────────────────────────────────

    @Test
    void givenTwoLoans_whenGetAll_thenReturns200WithBothRecords() throws Exception {
        createLoan("Loan A");
        createLoan("Loan B");

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenNoLoans_whenGetAll_thenReturns200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── (c) GET by id → 200 full detail ──────────────────────────────────────

    @Test
    void givenExistingLoan_whenGetById_thenReturns200WithFullDetail() throws Exception {
        Long id = createAndGetId("Detailed Loan");

        mockMvc.perform(get("/api/loans/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Detailed Loan"))
                .andExpect(jsonPath("$.forecastResults").exists())
                .andExpect(jsonPath("$.monthlyProjection").isArray());
    }

    // ── (d) GET unknown id → 404 with error body ──────────────────────────────

    @Test
    void givenNonExistentId_whenGetById_thenReturns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/api/loans/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── (e) PUT updates loan and returns recalculated response ────────────────

    @Test
    void givenExistingLoan_whenUpdate_thenReturns200WithRecalculatedResult() throws Exception {
        Long id = createAndGetId("Original Loan");

        CarLoanRequest updated = buildRequest("Updated Loan", null,
                bd("400000"), bd("50000"), bd("0"), bd("100"),
                bd("0"), 48, bd("10"));

        mockMvc.perform(put("/api/loans/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Updated Loan"))
                .andExpect(jsonPath("$.forecastResults.financedAmount").value(350000.00))
                .andExpect(jsonPath("$.monthlyProjection.length()").value(48));
    }

    // ── (f) DELETE → 204, subsequent GET → 404 ───────────────────────────────

    @Test
    void givenExistingLoan_whenDelete_thenReturns204AndSubsequentGetReturns404() throws Exception {
        Long id = createAndGetId("To Delete");

        mockMvc.perform(delete("/api/loans/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/loans/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ── (g) initialDeposit > purchasePrice → 400 ──────────────────────────────

    @Test
    void givenDepositExceedsPurchasePrice_whenCreate_thenReturns400() throws Exception {
        CarLoanRequest req = buildRequest("Bad Loan", null,
                bd("100000"), bd("150000"), bd("0"), bd("0"),
                bd("0"), 12, bd("10"));

        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── (h) balloonPayment > financedAmount → 400 ────────────────────────────

    @Test
    void givenBalloonExceedsFinancedAmount_whenCreate_thenReturns400() throws Exception {
        // financedAmount = 100000 - 0 + 0 = 100000; balloon = 200000
        CarLoanRequest req = buildRequest("Balloon Too Big", null,
                bd("100000"), bd("0"), bd("0"), bd("0"),
                bd("200000"), 12, bd("10"));

        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── (i) blank title → 400 ─────────────────────────────────────────────────

    @Test
    void givenBlankTitle_whenCreate_thenReturns400() throws Exception {
        CarLoanRequest req = buildRequest("", null,
                bd("100000"), bd("0"), bd("0"), bd("0"),
                bd("0"), 12, bd("10"));

        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── (j) negative interest rate → 400 from bean validation ────────────────

    @Test
    void givenNegativeInterestRate_whenCreate_thenReturns400() throws Exception {
        CarLoanRequest req = buildRequest("Bad Rate", null,
                bd("100000"), bd("0"), bd("0"), bd("0"),
                bd("0"), 12, bd("-1"));

        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    private CarLoanRequest buildRequest(String title, String description,
                                         BigDecimal purchasePrice, BigDecimal initialDeposit,
                                         BigDecimal onceOffFee, BigDecimal adminFee,
                                         BigDecimal balloonPayment, int termMonths,
                                         BigDecimal interestRate) {
        CarLoanRequest req = new CarLoanRequest();
        req.setTitle(title);
        req.setDescription(description);
        req.setPurchasePrice(purchasePrice);
        req.setInitialDeposit(initialDeposit);
        req.setOnceOffFee(onceOffFee);
        req.setAdminFee(adminFee);
        req.setBalloonPayment(balloonPayment);
        req.setTermMonths(termMonths);
        req.setInterestRate(interestRate);
        return req;
    }

    private void createLoan(String title) throws Exception {
        CarLoanRequest req = buildRequest(title, null,
                bd("200000"), bd("20000"), bd("0"), bd("69"),
                bd("0"), 24, bd("12"));
        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }

    private Long createAndGetId(String title) throws Exception {
        CarLoanRequest req = buildRequest(title, null,
                bd("200000"), bd("20000"), bd("0"), bd("69"),
                bd("0"), 24, bd("12"));
        MvcResult result = mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
