package com.personal.store_api.controller;

import com.personal.store_api.entity.Order;
import com.personal.store_api.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for OrderController.
 * Note: Checkout and payment tests are handled separately (Stripe is mocked).
 */
class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /orders/my-orders - should return current user's orders")
    void getMyOrders_withValidToken_shouldReturnOrders() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "orders.my_orders");

        // Create test orders
        testDataUtil.createOrder(user, BigDecimal.valueOf(100000), Order.Status.PENDING);
        testDataUtil.createOrder(user, BigDecimal.valueOf(200000), Order.Status.PAID);

        // When & Then
        mockMvc.perform(get("/orders/my-orders")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.page").value(0));
    }

    @Test
    @DisplayName("GET /orders/my-orders - should return empty list when no orders")
    void getMyOrders_noOrders_shouldReturnEmptyList() throws Exception {
        // Given
        var user = testDataUtil.createUser("test2@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "orders.my_orders");

        // When & Then
        mockMvc.perform(get("/orders/my-orders")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.items").isEmpty());
    }

    @Test
    @DisplayName("GET /orders/admin/all - should return all orders for admin")
    void getAllOrders_withAdminToken_shouldReturnAllOrders() throws Exception {
        // Given
        var admin = testDataUtil.createAdminUser("admin@example.com");
        String token = testDataUtil.getAuthorizationHeader(admin, "orders.admin_all");

        var user1 = testDataUtil.createUser("user1@example.com");
        var user2 = testDataUtil.createUser("user2@example.com");

        testDataUtil.createOrder(user1, BigDecimal.valueOf(100000), Order.Status.PENDING);
        testDataUtil.createOrder(user2, BigDecimal.valueOf(200000), Order.Status.PAID);

        // When & Then
        mockMvc.perform(get("/orders/admin/all")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.totalItems").value(2));
    }

    @Test
    @DisplayName("GET /orders/admin/all - should return 403 for non-admin user")
    void getAllOrders_withNonAdminToken_shouldReturnForbidden() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "orders.my_orders");

        // When & Then
        mockMvc.perform(get("/orders/admin/all")
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /orders/{orderId} - should return order by ID")
    void getOrderById_withValidToken_shouldReturnOrder() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "orders.read");
        Order order = testDataUtil.createOrder(user, BigDecimal.valueOf(100000), Order.Status.PENDING);

        // When & Then
        mockMvc.perform(get("/orders/" + order.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(order.getId()))
                .andExpect(jsonPath("$.result.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /orders/{orderId} - should return 400 for non-existent order")
    void getOrderById_nonExistentId_shouldReturnNotFound() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "orders.read");

        // When & Then
        mockMvc.perform(get("/orders/99999")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /orders/my-orders - should paginate correctly")
    void getMyOrders_withPagination_shouldReturnCorrectPage() throws Exception {
        // Given
        var user = testDataUtil.createUser("pagination@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "orders.my_orders");

        // Create 15 orders
        for (int i = 0; i < 15; i++) {
            testDataUtil.createOrder(user, BigDecimal.valueOf(100000 * (i + 1)), Order.Status.PENDING);
        }

        // Get first page (size 10)
        mockMvc.perform(get("/orders/my-orders")
                        .header("Authorization", token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.size").value(10))
                .andExpect(jsonPath("$.result.hasNext").value(true));

        // Get second page (size 10)
        mockMvc.perform(get("/orders/my-orders")
                        .header("Authorization", token)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.items").isArray())
                .andExpect(jsonPath("$.result.size").value(10))
                .andExpect(jsonPath("$.result.hasNext").value(false));
    }
}
