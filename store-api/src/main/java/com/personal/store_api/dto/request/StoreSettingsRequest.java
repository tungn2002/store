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
public class StoreSettingsRequest {

    @NotBlank(message = "error.store.name.blank")
    @Size(min = 2, max = 200, message = "error.store.name.size")
    String name;

    @Size(max = 500, message = "error.store.address.size")
    String address;
}
