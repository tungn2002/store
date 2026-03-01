package com.personal.store_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {

    @NotBlank(message = "error.product.name.blank")
    @Size(min = 2, max = 100, message = "error.product.name.size")
    String name;

    String description;

    @NotNull(message = "error.product.category.blank")
    Integer categoryId;

    @NotNull(message = "error.product.brand.blank")
    Integer brandId;

    MultipartFile image;
}
