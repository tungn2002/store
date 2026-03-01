package com.personal.store_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrandRequest {

    @NotBlank(message = "error.brand.name.blank")
    @Size(min = 2, max = 100, message = "error.brand.name.size")
    String name;
}
