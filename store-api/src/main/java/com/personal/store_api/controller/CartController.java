package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.CartItemUpdateRequest;
import com.personal.store_api.dto.request.CartRequest;
import com.personal.store_api.dto.response.CartItemResponse;
import com.personal.store_api.dto.response.CartResponse;
import com.personal.store_api.dto.response.ProductVariantResponse;
import com.personal.store_api.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {

    CartService cartService;

    @GetMapping
    @PreAuthorize("hasAuthority('cart.read')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse response = cartService.getCart();

        ApiResponse<CartResponse> apiResponse = ApiResponse.<CartResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/items/{cartId}/variants")
    @PreAuthorize("hasAuthority('cart.read_variants')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getCartProductVariants(
            @PathVariable Integer cartId) {
        List<ProductVariantResponse> response = cartService.getCartProductVariants(cartId);

        ApiResponse<List<ProductVariantResponse>> apiResponse = ApiResponse.<List<ProductVariantResponse>>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('cart.add')")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @RequestBody @Valid CartRequest request) {
        CartItemResponse response = cartService.addToCart(request);

        ApiResponse<CartItemResponse> apiResponse = ApiResponse.<CartItemResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/items/{cartId}")
    @PreAuthorize("hasAuthority('cart.update')")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Integer cartId,
            @RequestBody @Valid CartItemUpdateRequest request) {
        CartItemResponse response = cartService.updateCartItem(cartId, request);

        ApiResponse<CartItemResponse> apiResponse = ApiResponse.<CartItemResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/items/{cartId}/variant")
    @PreAuthorize("hasAuthority('cart.update_variant')")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItemVariant(
            @PathVariable Integer cartId,
            @RequestParam Integer productVariantId) {
        CartItemResponse response = cartService.updateCartItemVariant(cartId, productVariantId);

        ApiResponse<CartItemResponse> apiResponse = ApiResponse.<CartItemResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/items/{cartId}")
    @PreAuthorize("hasAuthority('cart.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @PathVariable Integer cartId) {
        cartService.deleteCartItem(cartId);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Cart item deleted successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('cart.clear')")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .message("Cart cleared successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
