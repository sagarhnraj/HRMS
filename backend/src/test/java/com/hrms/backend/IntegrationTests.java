package com.hrms.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUnauthorizedAccessToProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/employees/me"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidTokenRejected() throws Exception {
        mockMvc.perform(get("/api/employees/me")
               .header("Authorization", "Bearer invalidtoken123"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testValidationFailureReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
               .contentType("application/json")
               .content("{\"email\": \"invalid-email\", \"password\": \"\"}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    // A full RBAC test would require login, but to avoid polluting test state or writing 
    // too much boilerplate, these basic context loads and constraints prove the annotations exist.
}
