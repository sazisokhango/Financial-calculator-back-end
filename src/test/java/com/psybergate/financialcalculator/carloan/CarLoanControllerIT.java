package com.psybergate.financialcalculator.carloan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psybergate.financialcalculator.dto.CarLoanRequest;
import com.psybergate.financialcalculator.dto.RegisterRequest;
import com.psybergate.financialcalculator.repository.CarLoanRepository;
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
class CarLoanControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CarLoanRepository carLoanRepository;
    @Autowired private UserRepository userRepository;

    private Long userAId;
    private Long userBId;

    @BeforeEach
    void setUp() throws Exception {
        carLoanRepository.deleteAll();
        userRepository.deleteAll();
        userAId = registerAndGetId("Saziso", "Khango", "saziso@example.com");
        userBId = registerAndGetId("John", "Doe", "john@example.com");
    }

    // ── (a) POST valid request → 201 with forecastResults and monthlyProjection ──

    @Test
    void givenValidRequest_whenCreate_thenReturns201WithForecastAndSchedule() throws Exception {
        CarLoanRequest req = buildRequest(userAId, "BMW Finance Plan", "Monthly plan",
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

    // ── (b) GET list by userId → 200 with only that user's loans ─────────────

    @Test
    void givenTwoLoansForUserA_whenGetAllByUserId_thenReturns200WithBothRecords() throws Exception {
        createLoanForUser(userAId, "Loan A");
        createLoanForUser(userAId, "Loan B");

        mockMvc.perform(get("/api/loans").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── (c) GET by id → 200 full detail ──────────────────────────────────────

    @Test
    void givenExistingLoan_whenGetById_thenReturns200WithFullDetail() throws Exception {
        Long id = createLoanForUserAndGetId(userAId, "Detailed Loan");

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
        Long id = createLoanForUserAndGetId(userAId, "Original Loan");

        CarLoanRequest updated = buildRequest(userAId, "Updated Loan", null,
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
        Long id = createLoanForUserAndGetId(userAId, "To Delete");

        mockMvc.perform(delete("/api/loans/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/loans/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ── (g) initialDeposit > purchasePrice → 400 ──────────────────────────────

    @Test
    void givenDepositExceedsPurchasePrice_whenCreate_thenReturns400() throws Exception {
        CarLoanRequest req = buildRequest(userAId, "Bad Loan", null,
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
        CarLoanRequest req = buildRequest(userAId, "Balloon Too Big", null,
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
        CarLoanRequest req = buildRequest(userAId, "", null,
                bd("100000"), bd("0"), bd("0"), bd("0"),
                bd("0"), 12, bd("10"));

        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── (j) negative interest rate → 400 ─────────────────────────────────────

    @Test
    void givenNegativeInterestRate_whenCreate_thenReturns400() throws Exception {
        CarLoanRequest req = buildRequest(userAId, "Bad Rate", null,
                bd("100000"), bd("0"), bd("0"), bd("0"),
                bd("0"), 12, bd("-1"));

        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── (k) missing userId param → 400 ───────────────────────────────────────

    @Test
    void givenNoUserId_whenGetAll_thenReturns400() throws Exception {
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isBadRequest());
    }

    // ── (l) unknown userId → 404 ─────────────────────────────────────────────

    @Test
    void givenUnknownUserId_whenGetAll_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/loans").param("userId", "99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    // ── (m) user with no loans → 200 empty array ─────────────────────────────

    @Test
    void givenUserWithNoLoans_whenGetAll_thenReturns200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/loans").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── (n) two users — list for A returns only A's loans ────────────────────

    @Test
    void givenLoansForTwoUsers_whenGetAllForUserA_thenOnlyUserALoansReturned() throws Exception {
        createLoanForUser(userAId, "A Loan");
        createLoanForUser(userBId, "B Loan");

        mockMvc.perform(get("/api/loans").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("A Loan"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    private Long registerAndGetId(String first, String last, String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(first, last, email))))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private CarLoanRequest buildRequest(Long userId, String title, String description,
                                         BigDecimal purchasePrice, BigDecimal initialDeposit,
                                         BigDecimal onceOffFee, BigDecimal adminFee,
                                         BigDecimal balloonPayment, int termMonths,
                                         BigDecimal interestRate) {
        CarLoanRequest req = new CarLoanRequest();
        req.setUserId(userId);
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

    private void createLoanForUser(Long userId, String title) throws Exception {
        CarLoanRequest req = buildRequest(userId, title, null,
                bd("200000"), bd("20000"), bd("0"), bd("69"),
                bd("0"), 24, bd("12"));
        mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }

    private Long createLoanForUserAndGetId(Long userId, String title) throws Exception {
        CarLoanRequest req = buildRequest(userId, title, null,
                bd("200000"), bd("20000"), bd("0"), bd("69"),
                bd("0"), 24, bd("12"));
        MvcResult result = mockMvc.perform(post("/api/loans").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
