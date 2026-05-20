package com.psybergate.financialcalculator.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psybergate.financialcalculator.dto.RegisterRequest;
import com.psybergate.financialcalculator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserRegistrationSpec {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // --- US1: New User Registers Successfully ---

    @Test
    void givenValidRequest_whenRegister_thenReturns201WithUserProfile() throws Exception {
        RegisterRequest request = new RegisterRequest("Saziso", "Khango", "saziso@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("Saziso"))
                .andExpect(jsonPath("$.lastName").value("Khango"))
                .andExpect(jsonPath("$.email").value("saziso@example.com"));
    }

    @Test
    void givenValidRequest_whenRegister_thenEmailStoredLowercase() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "Doe", "John@Example.COM");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    // --- US2: Duplicate Email Rejected ---

    @Test
    void givenDuplicateEmail_whenRegisterTwice_thenSecondReturns400() throws Exception {
        RegisterRequest first = new RegisterRequest("Saziso", "Khango", "test@example.com");
        RegisterRequest second = new RegisterRequest("Other", "User", "test@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void givenDuplicateEmailDifferentCase_whenRegister_thenReturns400() throws Exception {
        RegisterRequest first = new RegisterRequest("Saziso", "Khango", "user@example.com");
        RegisterRequest second = new RegisterRequest("Other", "User", "USER@EXAMPLE.COM");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    // --- US3: Invalid / Missing Fields Rejected ---

    @Test
    void givenBlankFirstName_whenRegister_thenReturns400() throws Exception {
        RegisterRequest request = new RegisterRequest("", "Khango", "saziso@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void givenWhitespaceOnlyFirstName_whenRegister_thenReturns400() throws Exception {
        RegisterRequest request = new RegisterRequest("   ", "Khango", "saziso@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenBlankLastName_whenRegister_thenReturns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Saziso", "", "saziso@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenInvalidEmailFormat_whenRegister_thenReturns400() throws Exception {
        RegisterRequest request = new RegisterRequest("Saziso", "Khango", "not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void givenEmptyBody_whenRegister_thenReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
