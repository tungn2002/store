package com.personal.store_api.controller;

import com.personal.store_api.entity.*;
import com.personal.store_api.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CartController.
 */
class CartControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("GET /cart/items - should return current user's cart items")
    void getCartItems_withValidToken_shouldReturnCartItems() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.read");

        // When & Then
        mockMvc.perform(get("/cart/items")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    @DisplayName("POST /cart/items - should add item to cart")
    void addToCart_withValidToken_shouldAddItemToCart() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.add");
        Category category = testDataUtil.createCategory("Test Category");
        Brand brand = testDataUtil.createBrand("Test Brand");
        Product product = testDataUtil.createProduct("Test Product", category, brand);
        ProductVariant variant = testDataUtil.createProductVariant(product, "M", "Red", 100);

        String request = """
                {
                    "productVariantId": %d,
                    "quantity": 2
                }
                """.formatted(variant.getId());

        // When & Then
        mockMvc.perform(post("/cart/items")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.productVariantId").value(variant.getId()))
                .andExpect(jsonPath("$.result.quantity").value(2));
    }

    @Test
    @DisplayName("POST /cart/items - should return 400 when product out of stock")
    void addToCart_productOutOfStock_shouldReturnBadRequest() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.add");
        Category category = testDataUtil.createCategory("Test Category");
        Brand brand = testDataUtil.createBrand("Test Brand");
        Product product = testDataUtil.createProduct("Test Product", category, brand);
        ProductVariant variant = testDataUtil.createProductVariant(product, "M", "Red", 0);

        String request = """
                {
                    "productVariantId": %d,
                    "quantity": 1
                }
                """.formatted(variant.getId());

        // When & Then
        mockMvc.perform(post("/cart/items")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /cart/items - should return error when item already exists in cart")
    void addToCart_itemAlreadyExists_shouldReturnError() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.add");
        Category category = testDataUtil.createCategory("Test Category");
        Brand brand = testDataUtil.createBrand("Test Brand");
        Product product = testDataUtil.createProduct("Test Product", category, brand);
        ProductVariant variant = testDataUtil.createProductVariant(product, "M", "Red", 100);

        // Add item first time
        String request = """
                {
                    "productVariantId": %d,
                    "quantity": 2
                }
                """.formatted(variant.getId());

        mockMvc.perform(post("/cart/items")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk());

        // Try to add same item again
        mockMvc.perform(post("/cart/items")
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PUT /cart/items/{cartId} - should update cart item quantity")
    void updateCartItem_withValidToken_shouldUpdateQuantity() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.update");
        Category category = testDataUtil.createCategory("Test Category");
        Brand brand = testDataUtil.createBrand("Test Brand");
        Product product = testDataUtil.createProduct("Test Product", category, brand);
        ProductVariant variant = testDataUtil.createProductVariant(product, "M", "Red", 100);
        Cart cartItem = testDataUtil.createCartItem(user, variant, 2);

        String request = """
                {
                    "quantity": 5
                }
                """;

        // When & Then
        mockMvc.perform(put("/cart/items/" + cartItem.getId())
                        .contentType(APPLICATION_JSON)
                        .content(request)
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.quantity").value(5));
    }

    @Test
    @DisplayName("PUT /cart/items/{cartId}/variant - should update cart item variant")
    void updateCartItemVariant_withValidToken_shouldUpdateVariant() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.update_variant");
        Category category = testDataUtil.createCategory("Test Category");
        Brand brand = testDataUtil.createBrand("Test Brand");
        Product product = testDataUtil.createProduct("Test Product", category, brand);
        ProductVariant variant1 = testDataUtil.createProductVariant(product, "M", "Red", 100);
        ProductVariant variant2 = testDataUtil.createProductVariant(product, "L", "Blue", 100);
        Cart cartItem = testDataUtil.createCartItem(user, variant1, 2);

        // When & Then
        mockMvc.perform(put("/cart/items/" + cartItem.getId() + "/variant")
                        .param("productVariantId", variant2.getId().toString())
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /cart/items/{cartId} - should delete cart item")
    void deleteCartItem_withValidToken_shouldDeleteCartItem() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.delete");
        Category category = testDataUtil.createCategory("Test Category");
        Brand brand = testDataUtil.createBrand("Test Brand");
        Product product = testDataUtil.createProduct("Test Product", category, brand);
        ProductVariant variant = testDataUtil.createProductVariant(product, "M", "Red", 100);
        Cart cartItem = testDataUtil.createCartItem(user, variant, 2);

        // When & Then - Just verify it doesn't throw error
        mockMvc.perform(delete("/cart/items/" + cartItem.getId())
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /cart/items/{cartId} - should return 404 for non-existent cart item")
    void deleteCartItem_nonExistentId_shouldReturnNotFound() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.delete");

        // When & Then
        mockMvc.perform(delete("/cart/items/99999")
                        .header("Authorization", token)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /cart/items/{cartId}/variants - should return product variants for cart")
    void getCartProductVariants_withValidToken_shouldReturnVariants() throws Exception {
        // Given
        var user = testDataUtil.createUser("test@example.com");
        String token = testDataUtil.getAuthorizationHeader(user, "cart.read_variants");
        Category category = testDataUtil.createCategory("Test Category");
        Brand brand = testDataUtil.createBrand("Test Brand");
        Product product = testDataUtil.createProduct("Test Product", category, brand);
        ProductVariant variant = testDataUtil.createProductVariant(product, "M", "Red", 100);
        Cart cartItem = testDataUtil.createCartItem(user, variant, 2);

        // When & Then
        mockMvc.perform(get("/cart/items/" + cartItem.getId() + "/variants")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray());
    }
}
