package com.personal.store_api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailResponse {
    Integer id;
    String name;
    String description;
    CategoryResponse category;
    BrandResponse brand;
    String image; // Main product image
    List<String> variantImages; // All variant images
    BigDecimal price; // Price from first available variant
    List<String> colors; // All available colors
    List<String> sizes; // All available sizes
    List<VariantPriceStock> prices; // All variant prices with stock info
    Integer totalStock; // Total stock from all variants

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class VariantPriceStock {
        Integer productVariantId;
        String color;
        String size;
        BigDecimal price;
        Integer stock;
    }
}
