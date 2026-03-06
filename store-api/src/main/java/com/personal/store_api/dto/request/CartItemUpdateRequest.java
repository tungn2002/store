package com.personal.store_api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemUpdateRequest {

    @NotNull(message = "error.cart.quantity.blank")
    @Min(value = 1, message = "error.cart.quantity.min")
    @Max(value = 100, message = "error.cart.quantity.max")
    Integer quantity;
}
