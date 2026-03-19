package com.personal.store_api.mapper;

import com.personal.store_api.dto.response.CartItemResponse;
import com.personal.store_api.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "productVariantId", source = "productVariant.id")
    @Mapping(target = "productName", expression = "java(cart.getProductVariant().getProduct() != null ? cart.getProductVariant().getProduct().getName() : \"Unknown\")")
    @Mapping(target = "variantImage", source = "productVariant.image")
    @Mapping(target = "size", source = "productVariant.size")
    @Mapping(target = "color", source = "productVariant.color")
    @Mapping(target = "price", expression = "java(cart.getProductVariant().getPrice() != null ? cart.getProductVariant().getPrice() : BigDecimal.ZERO)")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "stockQuantity", source = "productVariant.stockQuantity")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CartItemResponse toResponse(Cart cart);
}
