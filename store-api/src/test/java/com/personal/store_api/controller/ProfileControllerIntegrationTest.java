package com.personal.store_api.controller;

import com.personal.store_api.entity.User;
import com.personal.store_api.enums.Gender;
import com.personal.store_api.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProfileController.
 */
class ProfileControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("GET /profile - should return current user's profile")
    void getProfile_withValidToken_shouldReturnProfile() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "profile.read");

        // When & Then
        mockMvc.perform(get("/profile")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("GET /profile - should return 401 without valid token")
    void getProfile_withoutValidToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /profile - should update user's profile")
    void updateProfile_withValidToken_shouldUpdateProfile() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "profile.update");

        String request = """
                {
                    "name": "Updated Name",
                    "email": "updated@example.com",
                    "phoneNumber": "9876543210",
                    "dateOfBirth": "1990-01-01",
                    "gender": "FEMALE",
                    "address": "123 Updated Street"
                }
                """;

        // When & Then
        mockMvc.perform(put("/profile")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Updated Name"))
                .andExpect(jsonPath("$.result.phoneNumber").value("9876543210"));
    }

    @Test
    @DisplayName("PUT /profile - should return 400 for invalid request")
    void updateProfile_invalidRequest_shouldReturnBadRequest() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "profile.update");

        String request = """
                {
                    "name": ""
                }
                """;

        // When & Then
        mockMvc.perform(put("/profile")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /profile/change-password - should change password successfully")
    void changePassword_withValidToken_shouldChangePassword() throws Exception {
        // Given
        User user = User.builder()
                .email("changepass@example.com")
                .name("Change Pass User")
                .password(passwordEncoder.encode("oldPassword123"))
                .phoneNumber("0123456789")
                .gender(Gender.MALE)
                .roles(new HashSet<>())
                .build();
        user = userRepository.save(user);

        String token = testDataUtil.getAuthorizationHeader(user, "profile.change_password");

        String request = """
                {
                    "oldPassword": "oldPassword123",
                    "newPassword": "newPassword456!",
                    "confirmPassword": "newPassword456!"
                }
                """;

        // When & Then
        mockMvc.perform(put("/profile/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /profile/change-password - should return error for wrong old password")
    void changePassword_wrongOldPassword_shouldReturnError() throws Exception {
        // Given
        User user = User.builder()
                .email("wrongpass@example.com")
                .name("Wrong Pass User")
                .password(passwordEncoder.encode("correctPassword"))
                .phoneNumber("0123456789")
                .gender(Gender.MALE)
                .roles(new HashSet<>())
                .build();
        user = userRepository.save(user);

        String token = testDataUtil.getAuthorizationHeader(user, "profile.change_password");

        String request = """
                {
                    "oldPassword": "wrongPassword",
                    "newPassword": "newPassword456!",
                    "confirmPassword": "newPassword456!"
                }
                """;

        // When & Then
        mockMvc.perform(put("/profile/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /profile/change-password - should return 400 for weak new password")
    void changePassword_weakNewPassword_shouldReturnBadRequest() throws Exception {
        // Given
        User user = User.builder()
                .email("weakpass@example.com")
                .name("Weak Pass User")
                .password(passwordEncoder.encode("oldPassword123"))
                .phoneNumber("0123456789")
                .gender(Gender.MALE)
                .roles(new HashSet<>())
                .build();
        user = userRepository.save(user);

        String token = testDataUtil.getAuthorizationHeader(user, "profile.change_password");

        String request = """
                {
                    "oldPassword": "oldPassword123",
                    "newPassword": "123"
                }
                """;

        // When & Then
        mockMvc.perform(put("/profile/change-password")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
