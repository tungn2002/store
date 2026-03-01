package com.personal.store_api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {

    @NotNull(message = "error.product.variant.product.blank")
    Integer productId;

    String size;

    String color;

    MultipartFile image;

    @DecimalMin(value = "0.01", message = "error.product.variant.price.min")
    BigDecimal price;

    @Min(value = 0, message = "error.product.variant.stock.min")
    Integer stockQuantity;
}
