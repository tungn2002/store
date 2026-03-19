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
import com.personal.store_api.mapper.CartItemMapper;
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

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final ProductVariantMapper productVariantMapper;
    private final CartItemMapper cartItemMapper;

    private static final int CART_MAX_SIZE = 100;

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems() {
        User currentUser = getCurrentUser();
        return cartRepository.findAllByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(cartItemMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getCartProductVariants(Integer cartId) {
        Cart cart = findCartById(cartId);
        return productVariantRepository.findByProduct(cart.getProductVariant().getProduct())
                .stream()
                .map(productVariantMapper::toProductVariantResponse)
                .toList();
    }

    @Transactional
    public CartItemResponse addToCart(CartRequest request) {
        User currentUser = getCurrentUser();

        // Check if user already has 100 items in cart
        if (cartRepository.countByUser(currentUser) >= CART_MAX_SIZE) {
            throw new AppException(ErrorCode.CART_MAX_ITEMS_REACHED);
        }

        // Get product variant
        ProductVariant variant = findVariantById(request.getProductVariantId());

        checkStockQuantity(variant);

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
        return cartItemMapper.toResponse(savedCart);
    }

    @Transactional
    public CartItemResponse updateCartItem(Integer cartId, CartItemUpdateRequest request) {
        User currentUser = getCurrentUser();
        Cart cart = findCartById(cartId);
        verifyCartOwnership(cart, currentUser);

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

        return cartItemMapper.toResponse(cart);
    }

    @Transactional
    public CartItemResponse updateCartItemVariant(Integer cartId, Integer productVariantId) {
        User currentUser = getCurrentUser();
        Cart cart = findCartById(cartId);
        verifyCartOwnership(cart, currentUser);

        // Check if variant already exists in user's cart (excluding current item)
        Optional<Cart> existingCart = cartRepository.findByUserIdAndProductVariantId(
                currentUser.getId(), productVariantId);

        if (existingCart.isPresent() && !existingCart.get().getId().equals(cartId)) {
            throw new AppException(ErrorCode.CART_ITEM_ALREADY_EXISTS);
        }

        // Get new product variant
        ProductVariant newVariant = findVariantById(productVariantId);

        checkStockQuantity(newVariant);

        // Check if current quantity exceeds new variant stock
        if (cart.getQuantity() > newVariant.getStockQuantity()) {
            cart.setQuantity(newVariant.getStockQuantity());
        }

        // Update cart with new variant
        cart.setProductVariant(newVariant);

        return cartItemMapper.toResponse(cart);
    }

    @Transactional
    public void deleteCartItem(Integer cartId) {
        User currentUser = getCurrentUser();
        Cart cart = findCartById(cartId);
        verifyCartOwnership(cart, currentUser);

        cartRepository.deleteByUserIdAndId(currentUser.getId(), cartId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = jwt.getSubject();

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Cart findCartById(Integer cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    private ProductVariant findVariantById(Integer variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
    }

    private void verifyCartOwnership(Cart cart, User user) {
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void checkStockQuantity(ProductVariant variant) {
        if (variant.getStockQuantity() == null || variant.getStockQuantity() <= 0) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
    }
}
