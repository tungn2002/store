package com.personal.store_api.controller;

import com.personal.store_api.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StoreSettingsController.
 */
class StoreSettingsControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /store-settings - should return store settings with valid token")
    void getStoreSettings_withValidToken_shouldReturnSettings() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "store_settings.read");

        // When & Then
        mockMvc.perform(get("/store-settings")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("GET /store-settings - should return 401 without valid token")
    void getStoreSettings_withoutValidToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/store-settings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /store-settings - should update store settings with valid token")
    void updateStoreSettings_withValidToken_shouldUpdateSettings() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "store_settings.update");

        String request = """
                {
                    "name": "Updated Store Name",
                    "address": "123 Updated Street"
                }
                """;

        // When & Then
        mockMvc.perform(put("/store-settings")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /store-settings - should return 403 for non-admin user")
    void updateStoreSettings_nonAdminUser_shouldReturnForbidden() throws Exception {
        // Given
        var user = testDataUtil.createUser("user@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "store_settings.read");

        String request = """
                {
                    "name": "Updated Store Name"
                }
                """;

        // When & Then
        mockMvc.perform(put("/store-settings")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
