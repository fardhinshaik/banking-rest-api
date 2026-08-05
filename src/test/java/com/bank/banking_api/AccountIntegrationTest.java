package com.bank.banking_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateUserSuccess() throws Exception {
        Map<String, String> userRequest = new HashMap<>();
        userRequest.put("fullName", "Alice Johnson");
        userRequest.put("email", "alice.test@bank.com");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testDuplicateEmailReturnsBadRequest() throws Exception {
        Map<String, String> userRequest = new HashMap<>();
        userRequest.put("fullName", "Bob Smith");
        userRequest.put("email", "bob.duplicate@bank.com");

        // First creation succeeds
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)));

        // Duplicate email creation triggers DataIntegrityViolationException -> 400 Bad Request
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    public void testInvalidStatusReturnsBadRequest() throws Exception {
        // Passing invalid status string triggers HttpMessageNotReadableException -> 400 Bad Request
        mockMvc.perform(patch("/api/v1/accounts/ACC12345/status")
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }
}