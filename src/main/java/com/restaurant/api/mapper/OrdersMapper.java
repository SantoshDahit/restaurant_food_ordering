package com.restaurant.api.mapper;

import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.Orders;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class OrdersMapper extends BaseMapper<Orders, OrdersDto> {
    public OrdersMapper(ModelMapper modelMapper) {
        super(modelMapper, Orders.class);
    }

    public OrdersDto.Response toResponse(Orders entity) {
        return super.toDto(entity, OrdersDto.Response.class);
    }
}
