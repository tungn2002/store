package com.personal.store_api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    Integer id;
    Integer cartId;
    Integer productVariantId;
    String productName;
    String variantImage;
    String size;
    String color;
    BigDecimal price;
    Integer quantity;
    Integer stockQuantity;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
