package com.personal.store_api.controller;

import com.personal.store_api.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 */
class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /users - should return paginated users with valid token")
    void getUsers_withValidToken_shouldReturnPaginatedUsers() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "users.read");

        // Create test users
        testDataUtil.createUser("user1@example.com");
        testDataUtil.createUser("user2@example.com");

        // When & Then
        mockMvc.perform(get("/users")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.page").value(0));
    }

    @Test
    @DisplayName("GET /users - should filter users by email")
    void getUsers_withEmailFilter_shouldReturnFilteredUsers() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "users.read");

        testDataUtil.createUser("specific@example.com");
        testDataUtil.createUser("other@example.com");

        // When & Then
        mockMvc.perform(get("/users")
                        .header("Authorization", token)
                        .param("email", "specific@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray());
    }

    @Test
    @DisplayName("GET /users - should return 401 without valid token")
    void getUsers_withoutValidToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /users/{id} - should delete user with valid token")
    void deleteUser_withValidToken_shouldDeleteUser() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "users.delete");

        var userToDelete = testDataUtil.createUser("delete@example.com");

        // When & Then
        mockMvc.perform(delete("/users/" + userToDelete.getId())
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /users/{id} - should return 404 for non-existent user")
    void deleteUser_nonExistentId_shouldReturnNotFound() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "users.delete");

        // When & Then
        mockMvc.perform(delete("/users/non-existent-id")
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users - should paginate correctly")
    void getUsers_withPagination_shouldReturnCorrectPage() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "users.read");

        // Create 25 users to have 3 pages (page size 10)
        for (int i = 0; i < 25; i++) {
            testDataUtil.createUser("pagetest" + i + "@example.com");
        }

        // Get first page (size 10) - should have 10 items
        mockMvc.perform(get("/users")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.size").value(10));

        // Get second page (size 10)
        mockMvc.perform(get("/users")
                        .header("Authorization", token)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.size").value(10));

        // Get third page (size 10) - last page
        mockMvc.perform(get("/users")
                        .header("Authorization", token)
                        .param("page", "2")
                        .param("size", "10")
                        .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray());
    }
}
