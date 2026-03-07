package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.request.CheckoutRequest;
import com.personal.store_api.dto.response.CheckoutResponse;
import com.personal.store_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CheckoutController {

    OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutResponse>> createCheckoutSession(
            @RequestBody @Valid CheckoutRequest request,
            @RequestParam(defaultValue = "http://localhost:5173/order/success") String successUrl,
            @RequestParam(defaultValue = "http://localhost:5173/cart") String cancelUrl) {
        CheckoutResponse response = orderService.createCheckoutSession(request, successUrl, cancelUrl);

        ApiResponse<CheckoutResponse> apiResponse = ApiResponse.<CheckoutResponse>builder()
                .result(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
