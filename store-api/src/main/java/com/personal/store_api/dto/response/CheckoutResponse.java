package com.personal.store_api.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutResponse {
    String checkoutUrl;
    Integer orderId;
    String sessionId;
}
