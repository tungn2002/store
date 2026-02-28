package com.personal.store_api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreSettingsResponse {
    String id;
    String name;
    String address;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
