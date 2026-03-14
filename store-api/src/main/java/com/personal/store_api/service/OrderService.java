package com.personal.store_api.service;

import com.personal.store_api.dto.request.CheckoutRequest;
import com.personal.store_api.dto.response.CheckoutResponse;
import com.personal.store_api.dto.response.OrderResponse;
import com.personal.store_api.dto.response.OrderSummaryResponse;
import com.personal.store_api.dto.response.PaginatedResponse;
import com.personal.store_api.entity.Order;
import com.personal.store_api.entity.OrderItem;
import com.personal.store_api.entity.ProductVariant;
import com.personal.store_api.entity.User;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.OrderMapper;
import com.personal.store_api.repository.OrderItemRepository;
import com.personal.store_api.repository.OrderRepository;
import com.personal.store_api.repository.ProductVariantRepository;
import com.personal.store_api.repository.UserRepository;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StripeService stripeService;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public CheckoutResponse createCheckoutSession(CheckoutRequest request, String successUrl, String cancelUrl) {
        //1.Get user info from authentication context
        String userId = null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            userId = jwt.getSubject();
        }

        // Fetch user info from database for snapshot
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<StripeService.SessionLineItem> lineItems = new ArrayList<>();

        //2. First pass: validate stock
        for (CheckoutRequest.CheckoutItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new AppException("Product variant not found: " + itemRequest.getProductVariantId()));

            if (variant.getStockQuantity() == null || variant.getStockQuantity() < itemRequest.getQuantity()) {
                throw new AppException("Insufficient stock for variant: " + itemRequest.getProductVariantId());
            }
        }

        //3. Second pass: create order items and deduct stock
        for (CheckoutRequest.CheckoutItemRequest itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId()).get();

            OrderItem orderItem = OrderItem.builder()
                    .productName(variant.getProduct().getName())
                    .size(variant.getSize())
                    .color(variant.getColor())
                    .quantity(itemRequest.getQuantity())
                    .sellingPrice(variant.getPrice())
                    .build();
            orderItems.add(orderItem);

            BigDecimal itemTotal = variant.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            lineItems.add(new StripeService.SessionLineItem(variant.getProduct().getName(), itemRequest.getQuantity(), variant.getPrice().doubleValue()));

            // Deduct stock immediately
            variant.setStockQuantity(variant.getStockQuantity() - itemRequest.getQuantity());
            productVariantRepository.save(variant);
        }

        Order order = Order.builder()
                .status(Order.Status.PENDING)
                .totalAmount(totalAmount)
                .userId(userId)
                .customerName(user != null ? user.getName() : null)
                .customerPhone(user != null ? user.getPhoneNumber() : null)
                .customerEmail(user != null ? user.getEmail() : null)
                .build();

        // Save order first to get ID
        orderRepository.save(order);

        // Set order reference for each item and save separately
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            orderItemRepository.save(item);
        }
        
        order.setItems(orderItems);

        try {
            String checkoutUrl = stripeService.createCheckoutSession(
                    order.getId(),
                    order.getCustomerName(),
                    order.getCustomerEmail(),
                    lineItems,
                    totalAmount,
                    successUrl,
                    cancelUrl
            );

            return CheckoutResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .orderId(order.getId())
                    .build();
        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage());
            throw new AppException("Payment error: " + e.getMessage());
        }
    }

    @Transactional
    public void updateOrderStatus(Integer orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found: " + orderId));
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional
    public void restoreStock(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found: " + orderId));

        for (OrderItem item : order.getItems()) {
            ProductVariant variant = findVariantByOrderItem(item);
            if (variant != null && variant.getStockQuantity() != null) {
                variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                productVariantRepository.save(variant);
            }
        }
    }

    public Order getOrder(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found: " + orderId));
    }

    private ProductVariant findVariantByOrderItem(OrderItem item) {
        return productVariantRepository.findAll().stream()
                .filter(v -> v.getProduct().getName().equals(extractProductName(item.getProductName()))
                        && (v.getSize() == null || v.getSize().equals(item.getSize()))
                        && (v.getColor() == null || v.getColor().equals(item.getColor())))
                .findFirst()
                .orElse(null);
    }

    private String extractProductName(String fullProductName) {
        int sizeIndex = fullProductName.indexOf(" - Size:");
        return sizeIndex > 0 ? fullProductName.substring(0, sizeIndex) : fullProductName;
    }

    /**
     * Get orders for the current authenticated user with pagination (summary only, no items)
     */
    public PaginatedResponse<OrderSummaryResponse> getUserOrders(int page, int size) {
        // Get current authenticated user ID
        String userId = null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            userId = jwt.getSubject();
        }

        if (userId == null) {
            throw new AppException("User not authenticated");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        List<OrderSummaryResponse> orderResponses = orderPage.getContent().stream()
                .map(orderMapper::toOrderSummaryResponse)
                .toList();

        return PaginatedResponse.<OrderSummaryResponse>builder()
                .items(orderResponses)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalItems(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isFirst(orderPage.isFirst())
                .isLast(orderPage.isLast())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }

    private OrderResponse toOrderResponseWithItems(Order order) {
        OrderResponse response = orderMapper.toOrderResponse(order);
        response.setItems(orderMapper.toOrderItemResponseList(order.getItems()));
        return response;
    }

    public OrderResponse toOrderResponseWithItemsPublic(Order order) {
        return toOrderResponseWithItems(order);
    }

    /**
     * Get all orders for admin with pagination (summary only, no items)
     */
    public PaginatedResponse<OrderSummaryResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAll(pageable);

        List<OrderSummaryResponse> orderResponses = orderPage.getContent().stream()
                .map(orderMapper::toOrderSummaryResponse)
                .toList();

        return PaginatedResponse.<OrderSummaryResponse>builder()
                .items(orderResponses)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalItems(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isFirst(orderPage.isFirst())
                .isLast(orderPage.isLast())
                .hasNext(orderPage.hasNext())
                .hasPrevious(orderPage.hasPrevious())
                .build();
    }
}
