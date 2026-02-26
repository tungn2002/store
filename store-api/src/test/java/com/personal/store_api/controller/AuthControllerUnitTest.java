package com.personal.store_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.personal.store_api.dto.request.AuthenticationRequest;
import com.personal.store_api.dto.response.AuthenticationResponse;
import com.personal.store_api.security.JwtTokenProvider;
import com.personal.store_api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private String validAdminToken;
    private String validUserToken;

    @BeforeEach
    void setUp() throws JOSEException {
        validAdminToken = generateJwtToken("admin-1", Set.of("ROLE_ADMIN", "ROLE_USER"));
        validUserToken = generateJwtToken("user-1", Set.of("ROLE_USER"));
    }

    private String generateJwtToken(String userId, Set<String> roles) throws JOSEException {
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(userId)
                .issuer("test-issuer")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 3600000))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", String.join(" ", roles))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );
        signedJWT.sign(new MACSigner("fd0874c323c8f2fb6c59e865fb22ad5f70f43604ddecc1fb7ca2e2489d5d97c0".getBytes()));
        return signedJWT.serialize();
    }

    @Test
    @DisplayName("Login with wrong password should return unauthorized")
    void testLogin_WithWrongPassword_ShouldReturnUnauthorized() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("admin@test.com")
                .password("wrongPassword")
                .build();

        when(authService.authenticate(any())).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Login with non-existent email should return unauthorized")
    void testLogin_WithNonExistentEmail_ShouldReturnUnauthorized() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("nonexistent@test.com")
                .password("password123")
                .build();

        when(authService.authenticate(any())).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Login with valid admin credentials should return token")
    void testLogin_WithValidAdminCredentials_ShouldReturnToken() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("admin@test.com")
                .password("admin123")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(validAdminToken)
                .build();

        when(authService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").value(validAdminToken));
    }

    @Test
    @DisplayName("Login with valid user credentials should return token")
    void testLogin_WithValidUserCredentials_ShouldReturnToken() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@test.com")
                .password("user123")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(validUserToken)
                .build();

        when(authService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").value(validUserToken));
    }

    @Test
    @DisplayName("Protected API without token should return unauthorized")
    void testProtectedApi_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected API with valid token should return ok")
    @WithMockUser(authorities = "ROLE_USER")
    void testProtectedApi_WithValidToken_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/auth/token"))
                .andExpect(status().isOk())
                .andExpect(content().string("abc"));
    }

    @Test
    @DisplayName("Admin API with user token should return forbidden")
    @WithMockUser(authorities = "ROLE_USER")
    void testAdminApi_WithUserToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/auth/token2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin API with admin token should return ok")
    @WithMockUser(authorities = {"ROLE_ADMIN", "ROLE_USER"})
    void testAdminApi_WithAdminToken_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/auth/token2"))
                .andExpect(status().isOk())
                .andExpect(content().string("abc"));
    }

    @Test
    @DisplayName("Admin API without token should return unauthorized")
    void testAdminApi_WithoutToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/token2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid login and access protected API")
    void testSummary_ValidLoginAndAccess() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("admin@test.com")
                .password("admin123")
                .build();

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .token(validAdminToken)
                .build();

        when(authService.authenticate(any())).thenReturn(authResponse);
        when(jwtTokenProvider.verifyToken(validAdminToken)).thenReturn(SignedJWT.parse(validAdminToken));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").isNotEmpty());

        mockMvc.perform(get("/auth/token")
                        .header("Authorization", "Bearer " + validAdminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User cannot access admin API")
    @WithMockUser(authorities = "ROLE_USER")
    void testSummary_UserCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/auth/token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/auth/token2"))
                .andExpect(status().isForbidden());
    }
}
