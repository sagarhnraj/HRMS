package com.hrms.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hrms.backend.entity.*;
import com.hrms.backend.repository.*;
import com.hrms.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
}
