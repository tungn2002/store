package com.personal.store_api.controller;

import com.personal.store_api.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BrandController.
 */
class BrandControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /brands - should return paginated brands with valid token")
    void getBrands_withValidToken_shouldReturnPaginatedBrands() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.read");

        // When & Then
        mockMvc.perform(get("/brands")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.page").value(0))
                .andExpect(jsonPath("$.result.size").value(10));
    }

    @Test
    @DisplayName("GET /brands - should return 401 without valid token")
    void getBrands_withoutValidToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/brands"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /brands/all - should return all brands")
    void getAllBrands_withValidToken_shouldReturnAllBrands() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.read");

        // When & Then
        mockMvc.perform(get("/brands/all")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    @DisplayName("POST /brands - should create new brand with valid token")
    void createBrand_withValidToken_shouldCreateBrand() throws Exception {
        // Given
        var user = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.create");

        String request = """
                {
                    "name": "Test Brand"
                }
                """;

        // When & Then
        mockMvc.perform(post("/brands")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Test Brand"));
    }

    @Test
    @DisplayName("POST /brands - should return 400 for invalid request")
    void createBrand_invalidRequest_shouldReturnBadRequest() throws Exception {
        // Given
        var user = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.create");

        String request = """
                {
                    "name": ""
                }
                """;

        // When & Then
        mockMvc.perform(post("/brands")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /brands/{id} - should update existing brand")
    void updateBrand_withValidToken_shouldUpdateBrand() throws Exception {
        // Given
        var user = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.update");
        var brand = testDataUtil.createBrand("Original Brand");

        String request = """
                {
                    "name": "Updated Brand"
                }
                """;

        // When & Then
        mockMvc.perform(put("/brands/" + brand.getId())
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Updated Brand"));
    }

    @Test
    @DisplayName("PUT /brands/{id} - should return 404 for non-existent brand")
    void updateBrand_nonExistentId_shouldReturnNotFound() throws Exception {
        // Given
        var user = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.update");

        String request = """
                {
                    "name": "Updated Brand"
                }
                """;

        // When & Then
        mockMvc.perform(put("/brands/99999")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /brands/{id} - should delete existing brand")
    void deleteBrand_withValidToken_shouldDeleteBrand() throws Exception {
        // Given
        var user = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.delete");
        var brand = testDataUtil.createBrand("Brand to Delete");

        // When & Then
        mockMvc.perform(delete("/brands/" + brand.getId())
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /brands/{id} - should return 404 for non-existent brand")
    void deleteBrand_nonExistentId_shouldReturnNotFound() throws Exception {
        // Given
        var user = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "brands.delete");

        // When & Then
        mockMvc.perform(delete("/brands/99999")
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
