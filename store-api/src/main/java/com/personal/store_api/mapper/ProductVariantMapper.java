package com.personal.store_api.mapper;

import com.personal.store_api.dto.request.ProductVariantRequest;
import com.personal.store_api.dto.response.ProductVariantResponse;
import com.personal.store_api.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductVariantMapper {

    @Mapping(target = "productId", source = "product.id")
    ProductVariantResponse toProductVariantResponse(ProductVariant variant);

    @Mapping(target = "product.id", source = "productId")
    @Mapping(target = "image", ignore = true)
    void updateProductVariantFromRequest(@MappingTarget ProductVariant variant, ProductVariantRequest request);
}
