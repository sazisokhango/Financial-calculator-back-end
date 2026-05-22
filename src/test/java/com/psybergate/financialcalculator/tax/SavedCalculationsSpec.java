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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SavedCalculationsSpec {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private TaxCalculationRepository taxCalculationRepository;

    private String userAId;
    private String userBId;

    @BeforeEach
    void setUp() throws Exception {
        taxCalculationRepository.deleteAll();
        userRepository.deleteAll();
        userAId = registerAndGetId("Saziso", "Khango", "saziso@example.com");
        userBId = registerAndGetId("John", "Doe", "john@example.com");
    }

    // ── US1: View All Saved Calculations ─────────────────────────────────────

    @Test
    void givenTwoCalcsForUserA_whenGetAll_thenReturns200WithBothRecords() throws Exception {
        saveCalc(userAId, "Calc 1", "500000", 35);
        saveCalc(userAId, "Calc 2", "400000", 40);

        mockMvc.perform(get("/api/tax").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void givenNoCalcsForUser_whenGetAll_thenReturns200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/tax").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenCalcsForBothUsers_whenGetAllForUserA_thenOnlyUserARecordsReturned() throws Exception {
        saveCalc(userAId, "A Calc", "500000", 35);
        saveCalc(userBId, "B Calc", "300000", 40);

        mockMvc.perform(get("/api/tax").param("userId", userAId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(userAId));
    }

    @Test
    void givenNonExistentUserId_whenGetAll_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/tax").param("userId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ── US2: View Single Calculation ──────────────────────────────────────────

    @Test
    void givenExistingCalc_whenGetById_thenReturns200WithFullRecord() throws Exception {
        Long id = saveCalcAndGetId(userAId, "My Tax", "500000", 35);

        mockMvc.perform(get("/api/tax/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("My Tax"))
                .andExpect(jsonPath("$.taxBeforeRebate").value(117507.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(100272.00));
    }

    @Test
    void givenNonExistentId_whenGetById_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/tax/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Calculation not found"));
    }

    // ── US3: Update a Saved Calculation ──────────────────────────────────────

    @Test
    void givenExistingCalc_whenUpdateWithNewSalary_thenReturns200WithRecalculatedBreakdown() throws Exception {
        Long id = saveCalcAndGetId(userAId, "Original", "500000", 35);

        TaxCalculationRequest update = buildRequest(userAId, "Updated Tax", "600000", 35);

        mockMvc.perform(put("/api/tax/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Updated Tax"))
                .andExpect(jsonPath("$.salary").value(600000.00))
                .andExpect(jsonPath("$.taxBeforeRebate").value(152867.00))
                .andExpect(jsonPath("$.finalTaxLiability").value(135632.00));
    }

    @Test
    void givenNegativeSalaryInUpdate_whenPut_thenReturns400() throws Exception {
        Long id = saveCalcAndGetId(userAId, "Test", "500000", 35);
        TaxCalculationRequest bad = buildRequest(userAId, "Bad", "-1000", 35);

        mockMvc.perform(put("/api/tax/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentId_whenPut_thenReturns404() throws Exception {
        TaxCalculationRequest req = buildRequest(userAId, "Test", "500000", 35);

        mockMvc.perform(put("/api/tax/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Calculation not found"));
    }

    // ── US4: Delete a Saved Calculation ──────────────────────────────────────

    @Test
    void givenExistingCalc_whenDelete_thenReturns204() throws Exception {
        Long id = saveCalcAndGetId(userAId, "To Delete", "500000", 35);

        mockMvc.perform(delete("/api/tax/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenDeletedCalc_whenGetById_thenReturns404() throws Exception {
        Long id = saveCalcAndGetId(userAId, "Gone", "500000", 35);

        mockMvc.perform(delete("/api/tax/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tax/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Calculation not found"));
    }

    @Test
    void givenNonExistentId_whenDelete_thenReturns404() throws Exception {
        mockMvc.perform(delete("/api/tax/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Calculation not found"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String registerAndGetId(String first, String last, String email) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(first, last, email))))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("email").toString();
    }

    private void saveCalc(String userId, String title, String salary, int age) throws Exception {
        mockMvc.perform(post("/api/tax")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildRequest(userId, title, salary, age))));
    }

    private Long saveCalcAndGetId(String userId, String title, String salary, int age) throws Exception {
        MvcResult r = mockMvc.perform(post("/api/tax")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(userId, title, salary, age))))
                .andReturn();
        return objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
    }

    private TaxCalculationRequest buildRequest(String userId, String title, String salary, int age) {
        TaxCalculationRequest r = new TaxCalculationRequest();
        r.setUserId(userId);
        r.setTitle(title);
        r.setSalary(new BigDecimal(salary));
        r.setAge(age);
        return r;
    }
}
