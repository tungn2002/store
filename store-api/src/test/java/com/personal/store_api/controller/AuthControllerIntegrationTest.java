package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.store_api.dto.request.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AuthController
 * 
 * Tests cover:
 * 1. Login API - invalid credentials, valid admin credentials
 * 2. JWT Authentication - valid JWT token
 * 3. Authorization - ADMIN role required endpoint with proper/improper permissions
 * 
 * Note: Uses H2 in-memory database with automatic rollback via @Transactional
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.show-sql=false",
        "spring.h2.console.enabled=false"
})
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== LOGIN TESTS ====================

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnError() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("wrong@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(400));
    }

    @Test
    void testLogin_WithValidAdminCredentials_ShouldReturnToken() throws Exception {
        // Default admin credentials from ApplicationInitConfig
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("admin@example.com")
                .password("admin")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").exists())
                .andExpect(jsonPath("$.result.token").isString());
    }

    // ==================== JWT TOKEN TESTS ====================

    @Test
    void testToken_WithValidJwt_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/auth/token")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "USER"))))
                .andExpect(status().isOk());
    }

    // ==================== AUTHORIZATION TESTS (ADMIN ROLE) ====================

    @Test
    void testToken2_WithAdminRoleAndValidJwt_ShouldReturnSuccess() throws Exception {
        // Use authorities() instead of claim() to properly set Spring Security authorities
        mockMvc.perform(get("/auth/token2")
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void testToken2_WithUserRoleAndValidJwt_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/auth/token2")
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void testToken2_WithoutJwt_ShouldReturnUnauthorized() throws Exception {
        // Request without Authorization header
        mockMvc.perform(get("/auth/token2"))
                .andExpect(status().isUnauthorized());
    }
}
