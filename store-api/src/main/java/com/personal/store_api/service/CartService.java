package com.personal.store_api.service;

import com.personal.store_api.dto.request.CartItemUpdateRequest;
import com.personal.store_api.dto.request.CartRequest;
import com.personal.store_api.dto.response.CartItemResponse;
import com.personal.store_api.dto.response.ProductVariantResponse;
import com.personal.store_api.entity.Cart;
import com.personal.store_api.entity.Product;
import com.personal.store_api.entity.ProductVariant;
import com.personal.store_api.entity.User;
import com.personal.store_api.enums.ErrorCode;
import com.personal.store_api.exception.AppException;
import com.personal.store_api.mapper.ProductVariantMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    final CartRepository cartRepository;
    final ProductVariantRepository productVariantRepository;
    final UserRepository userRepository;
    final ProductVariantMapper productVariantMapper;

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

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems() {
        User currentUser = getCurrentUser();
        List<Cart> carts = cartRepository.findAllByUser(currentUser);

        return carts.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getCartProductVariants(Integer cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        ProductVariant variant = cart.getProductVariant();
        Product product = variant.getProduct();

        // Get all variants for this product
        return productVariantRepository.findByProduct(product)
                .stream()
                .map(productVariantMapper::toProductVariantResponse)
                .toList();
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

    @Transactional
    public CartItemResponse updateCartItem(Integer cartId, CartItemUpdateRequest request) {
        User currentUser = getCurrentUser();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Verify ownership
        if (!cart.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        ProductVariant variant = cart.getProductVariant();

        // Validate quantity
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                throw new AppException(ErrorCode.CART_QUANTITY_MIN);
            }

            if (request.getQuantity() > variant.getStockQuantity()) {
                throw new AppException(ErrorCode.CART_QUANTITY_EXCEEDS_STOCK);
            }

            cart.setQuantity(request.getQuantity());
        }

        Cart updatedCart = cartRepository.save(cart);
        return toCartItemResponse(updatedCart);
    }

    @Transactional
    public CartItemResponse updateCartItemVariant(Integer cartId, Integer productVariantId) {
        User currentUser = getCurrentUser();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Verify ownership
        if (!cart.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Check if variant already exists in user's cart (excluding current item)
        Optional<Cart> existingCart = cartRepository.findByUserIdAndProductVariantId(
                currentUser.getId(), productVariantId);

        if (existingCart.isPresent() && !existingCart.get().getId().equals(cartId)) {
            throw new AppException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }

        // Get new product variant
        ProductVariant newVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        // Check stock quantity
        if (newVariant.getStockQuantity() == null || newVariant.getStockQuantity() <= 0) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // Check if current quantity exceeds new variant stock
        if (cart.getQuantity() > newVariant.getStockQuantity()) {
            cart.setQuantity(newVariant.getStockQuantity());
        }

        // Update cart with new variant
        cart.setProductVariant(newVariant);
        Cart updatedCart = cartRepository.save(cart);

        return toCartItemResponse(updatedCart);
    }

    @Transactional
    public void deleteCartItem(Integer cartId) {
        User currentUser = getCurrentUser();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Verify ownership
        if (!cart.getUser().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        cartRepository.deleteByUserIdAndId(currentUser.getId(), cartId);
    }

    private CartItemResponse toCartItemResponse(Cart cart) {
        ProductVariant variant = cart.getProductVariant();
        BigDecimal price = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;

        return CartItemResponse.builder()
                .id(cart.getId())
                .cartId(cart.getId())
                .productVariantId(variant.getId())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : "Unknown")
                .variantImage(variant.getImage())
                .size(variant.getSize())
                .color(variant.getColor())
                .price(price)
                .quantity(cart.getQuantity())
                .stockQuantity(variant.getStockQuantity())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
