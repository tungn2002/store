package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.CheckoutRequest;
import com.personal.store_api.dto.response.CheckoutResponse;
import com.personal.store_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for checkout operations.
 */
@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final OrderService orderService;

    /**
     * Create Stripe checkout session for order payment.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('checkout.create')")
    public ResponseEntity<ApiResponse<CheckoutResponse>> createCheckoutSession(
            @RequestBody @Valid CheckoutRequest request,
            @RequestParam(defaultValue = "http://localhost:5173/order/success") String successUrl,
            @RequestParam(defaultValue = "http://localhost:5173/cart") String cancelUrl) {
        CheckoutResponse response = orderService.createCheckoutSession(request, successUrl, cancelUrl);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Build success response with result.
     */
    private <T> ApiResponse<T> buildResponse(T result) {
        return ApiResponse.<T>builder()
                .result(result)
                .build();
    }
}
