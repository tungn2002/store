package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.CartRequest;
import com.personal.store_api.dto.response.CartItemResponse;
import com.personal.store_api.service.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {

    CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @RequestBody @Valid CartRequest request) {
        CartItemResponse response = cartService.addToCart(request);

        ApiResponse<CartItemResponse> apiResponse = ApiResponse.<CartItemResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
