package com.personal.store_api.service;

import com.personal.store_api.dto.request.CartRequest;
import com.personal.store_api.dto.response.CartItemResponse;
import com.personal.store_api.entity.Cart;
import com.personal.store_api.entity.ProductVariant;
import com.personal.store_api.entity.User;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.repository.CartRepository;
import com.personal.store_api.repository.ProductVariantRepository;
import com.personal.store_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    final CartRepository cartRepository;
    final ProductVariantRepository productVariantRepository;
    final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        Object principal = authentication.getPrincipal();
        String userId;
        
        if (principal instanceof Jwt jwt) {
            userId = jwt.getSubject();
        } else {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public CartItemResponse addToCart(CartRequest request) {
        User currentUser = getCurrentUser();

        // Check if user already has 100 items in cart
        int currentCartSize = cartRepository.countByUser(currentUser);
        if (currentCartSize >= 100) {
            throw new AppException(ErrorCode.CART_MAX_ITEMS_REACHED);
        }

        // Get product variant
        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        // Check stock quantity
        if (variant.getStockQuantity() == null || variant.getStockQuantity() <= 0) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // Check if requested quantity exceeds available stock
        if (request.getQuantity() > variant.getStockQuantity()) {
            throw new AppException(ErrorCode.CART_QUANTITY_EXCEEDS_STOCK);
        }

        // Check if item already exists in cart - if exists, throw error
        Optional<Cart> existingCart = cartRepository.findByUserIdAndProductVariantId(
                currentUser.getId(), request.getProductVariantId());

        if (existingCart.isPresent()) {
            throw new AppException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }

        // Create new cart item
        Cart cart = Cart.builder()
                .user(currentUser)
                .productVariant(variant)
                .quantity(request.getQuantity())
                .build();

        Cart savedCart = cartRepository.save(cart);
        return toCartItemResponse(savedCart);
    }

    private CartItemResponse toCartItemResponse(Cart cart) {
        ProductVariant variant = cart.getProductVariant();
        BigDecimal price = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;

        return CartItemResponse.builder()
                .id(cart.getId())
                .productVariantId(variant.getId())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : "Unknown")
                .variantImage(variant.getImage())
                .size(variant.getSize())
                .color(variant.getColor())
                .price(price)
                .quantity(cart.getQuantity())
                .stockQuantity(variant.getStockQuantity())
                .build();
    }
}
