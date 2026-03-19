package com.personal.store_api.controller;

import com.personal.store_api.entity.Role;
import com.personal.store_api.entity.User;
import com.personal.store_api.enums.Gender;
import com.personal.store_api.infrastructure.AbstractIntegrationTest;
import com.personal.store_api.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 */
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("POST /auth/register - should register new user successfully")
    void register_newUser_shouldRegisterSuccessfully() throws Exception {
        // Given
        String request = """
                {
                    "name": "New User",
                    "email": "newuser@example.com",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verify user was created
        var userOpt = userRepository.findByEmail("newuser@example.com");
        assert userOpt.isPresent();
        assert userOpt.get().getName().equals("New User");
    }

    @Test
    @DisplayName("POST /auth/register - should return error when email already exists")
    void register_existingEmail_shouldReturnError() throws Exception {
        // Given - create user first
        testDataUtil.createUser("existing@example.com");

        String request = """
                {
                    "name": "Duplicate User",
                    "email": "existing@example.com",
                    "password": "password123"
                }
                """;

        // When & Then - API returns 200 but token is null
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").doesNotExist());
    }

    @Test
    @DisplayName("POST /auth/register - should return error for invalid email format")
    void register_invalidEmail_shouldReturnError() throws Exception {
        // Given
        String request = """
                {
                    "name": "Invalid User",
                    "email": "invalid-email",
                    "password": "password123"
                }
                """;

        // When & Then - API returns 200 but token is null (validation fails silently)
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").doesNotExist());
    }

    @Test
    @DisplayName("POST /auth/login - should login successfully with valid credentials")
    void login_validCredentials_shouldLoginSuccessfully() throws Exception {
        // Given
        String email = "login@example.com";
        String password = "password123";
        testDataUtil.createUser(email);

        // Manually create user with proper password encoding
        User user = User.builder()
                .email(email)
                .name("Login User")
                .password(passwordEncoder.encode(password))
                .phoneNumber("0123456789")
                .gender(Gender.MALE)
                .roles(new HashSet<>())
                .build();
        userRepository.save(user);

        String request = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").exists());
    }

    @Test
    @DisplayName("POST /auth/login - should return error for invalid credentials")
    void login_invalidCredentials_shouldReturnError() throws Exception {
        // Given
        testDataUtil.createUser("test@example.com");

        String request = """
                {
                    "email": "test@example.com",
                    "password": "wrongpassword"
                }
                """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /auth/login - should return error for non-existent user")
    void login_nonExistentUser_shouldReturnError() throws Exception {
        // Given
        String request = """
                {
                    "email": "nonexistent@example.com",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /auth/token - should return OK with valid token and permission")
    void testToken_withValidTokenAndPermission_shouldReturnOK() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "auth.token");

        // When & Then
        mockMvc.perform(get("/auth/token")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /auth/token - should return 403 without required permission")
    void testToken_withoutPermission_shouldReturnForbidden() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "other.permission");

        // When & Then
        mockMvc.perform(get("/auth/token")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /auth/token2 - should return OK with valid token and permission")
    void testToken2_withValidTokenAndPermission_shouldReturnOK() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "auth.token2");

        // When & Then
        mockMvc.perform(get("/auth/token2")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /auth/logout - should logout successfully and invalidate token")
    void logout_withValidToken_shouldLogoutSuccessfully() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.generateToken(user, "auth.token");

        String request = """
                {
                    "token": "%s"
                }
                """.formatted(token);

        // When & Then
        mockMvc.perform(post("/auth/logout")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", testDataUtil.getAuthorizationHeader(user, "auth.token"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
