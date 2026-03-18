package com.personal.store_api.controller;

import com.personal.store_api.dto.ApiResponse;
import com.personal.store_api.dto.response.OrderResponse;
import com.personal.store_api.dto.response.OrderSummaryResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for order management operations.
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Get orders for the current authenticated user with pagination (summary only, no items).
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('orders.my_orders')")
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderSummaryResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<OrderSummaryResponse> response = orderService.getUserOrders(page, size);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get all orders for admin with pagination (summary only, no items).
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('orders.admin_all')")
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderSummaryResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<OrderSummaryResponse> response = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(buildResponse(response));
    }

    /**
     * Get order details by ID (includes items).
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('orders.read')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Integer orderId) {
        OrderResponse order = orderService.toOrderResponseWithItemsPublic(orderService.getOrder(orderId));
        return ResponseEntity.ok(buildResponse(order));
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
