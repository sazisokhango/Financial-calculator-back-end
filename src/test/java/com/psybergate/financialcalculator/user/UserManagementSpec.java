package com.psybergate.financialcalculator.user;

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
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserManagementSpec {

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

    // --- US1: Browse All Registered Users ---

    @Test
    void givenTwoUsers_whenGetAllUsers_thenReturns200WithBothUsers() throws Exception {
        register("Saziso", "Khango", "saziso@example.com");
        register("John", "Doe", "john@example.com");

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].id").isNumber());
    }

    @Test
    void givenNoUsers_whenGetAllUsers_thenReturns200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void givenOneUser_whenGetAllUsers_thenResponseContainsCorrectFields() throws Exception {
        register("Saziso", "Khango", "saziso@example.com");

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Saziso"))
                .andExpect(jsonPath("$[0].lastName").value("Khango"))
                .andExpect(jsonPath("$[0].email").value("saziso@example.com"));
    }

    // --- US2: View Specific User by ID ---

    @Test
    void givenExistingUser_whenGetById_thenReturns200WithCorrectProfile() throws Exception {
        Long id = registerAndGetId("Saziso", "Khango", "saziso@example.com");

        mockMvc.perform(get("/api/user/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.firstName").value("Saziso"))
                .andExpect(jsonPath("$.lastName").value("Khango"))
                .andExpect(jsonPath("$.email").value("saziso@example.com"));
    }

    @Test
    void givenNonExistentId_whenGetById_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/user/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // --- Helpers ---

    private void register(String firstName, String lastName, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest(firstName, lastName, email))));
    }

    private Long registerAndGetId(String firstName, String lastName, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(firstName, lastName, email))))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
