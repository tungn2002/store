package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.CartItemUpdateRequest;
import com.personal.store_api.dto.request.CartRequest;
import com.personal.store_api.dto.response.CartItemResponse;
import com.personal.store_api.dto.response.ProductVariantResponse;
import com.personal.store_api.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for cart management operations.
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Get current user's cart items.
     */
    @GetMapping("/items")
    @PreAuthorize("hasAuthority('cart.read')")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCartItems() {
        List<CartItemResponse> response = cartService.getCartItems();
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get product variants in a cart.
     */
    @GetMapping("/items/{cartId}/variants")
    @PreAuthorize("hasAuthority('cart.read_variants')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getCartProductVariants(
            @PathVariable Integer cartId) {
        List<ProductVariantResponse> response = cartService.getCartProductVariants(cartId);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Add item to cart.
     */
    @PostMapping("/items")
    @PreAuthorize("hasAuthority('cart.add')")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @RequestBody @Valid CartRequest request) {
        CartItemResponse response = cartService.addToCart(request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Update cart item quantity.
     */
    @PutMapping("/items/{cartId}")
    @PreAuthorize("hasAuthority('cart.update')")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Integer cartId,
            @RequestBody @Valid CartItemUpdateRequest request) {
        CartItemResponse response = cartService.updateCartItem(cartId, request);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Update cart item variant.
     */
    @PutMapping("/items/{cartId}/variant")
    @PreAuthorize("hasAuthority('cart.update_variant')")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItemVariant(
            @PathVariable Integer cartId,
            @RequestParam Integer productVariantId) {
        CartItemResponse response = cartService.updateCartItemVariant(cartId, productVariantId);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Delete item from cart.
     */
    @DeleteMapping("/items/{cartId}")
    @PreAuthorize("hasAuthority('cart.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @PathVariable Integer cartId) {
        cartService.deleteCartItem(cartId);
        return ResponseEntity.ok(buildResponse());
    }

    /**
     * Build success response with result.
     */
    private <T> ApiResponse<T> buildResponse(T result) {
        return ApiResponse.<T>builder()
                .result(result)
                .build();
    }

    /**
     * Build success response without result.
     */
    private ApiResponse<Void> buildResponse() {
        return ApiResponse.<Void>builder()
                .build();
    }
}
