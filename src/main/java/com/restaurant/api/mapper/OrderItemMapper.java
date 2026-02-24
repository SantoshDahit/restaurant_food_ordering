package com.restaurant.api.mapper;

import com.restaurant.api.dto.OrderItemDto;
import com.restaurant.api.entity.OrderItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper extends BaseMapper<OrderItem, OrderItemDto> {
    public OrderItemMapper(ModelMapper modelMapper) {
        super(modelMapper, OrderItem.class);
    }

    public OrderItemDto.Response toResponse(OrderItem entity) {
        return super.toDto(entity, OrderItemDto.Response.class);
    }
}
