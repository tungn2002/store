package com.personal.store_api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Integer id;
    String name;
    String description;
    CategoryResponse category;
    BrandResponse brand;
    String image;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<ProductVariantResponse> variants;
}
