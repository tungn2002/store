package com.personal.store_api.mapper;

import com.personal.store_api.dto.response.OrderResponse;
import com.personal.store_api.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "userId")
    OrderResponse toOrderResponse(Order order);
}
