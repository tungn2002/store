package com.personal.store_api.mapper;

import com.personal.store_api.dto.response.OrderItemResponse;
import com.personal.store_api.dto.response.OrderResponse;
import com.personal.store_api.dto.response.OrderSummaryResponse;
import com.personal.store_api.entity.Order;
import com.personal.store_api.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "userId")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "customerId", source = "userId")
    OrderSummaryResponse toOrderSummaryResponse(Order order);

    default List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    @Mapping(target = "id", source = "id")
    OrderItemResponse toOrderItemResponse(OrderItem item);
}
