package com.personal.store_api.mapper;

import com.personal.store_api.dto.request.ProductRequest;
import com.personal.store_api.dto.response.ProductResponse;
import com.personal.store_api.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "category.id", source = "category.id")
    @Mapping(target = "category.name", source = "category.name")
    @Mapping(target = "brand.id", source = "brand.id")
    @Mapping(target = "brand.name", source = "brand.name")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "brand.id", source = "brandId")
    @Mapping(target = "image", ignore = true)
    void updateProductFromRequest(@MappingTarget Product product, ProductRequest request);
}
