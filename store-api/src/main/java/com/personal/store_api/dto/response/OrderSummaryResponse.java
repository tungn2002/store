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
public class OrderSummaryResponse {
    Integer id;
    String status;
    BigDecimal totalAmount;
    String customerId;
    String customerName;
    String customerPhone;
    String customerEmail;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
