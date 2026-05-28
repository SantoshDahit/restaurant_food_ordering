package com.restaurant.api.mapper;

import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.Orders;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrdersMapper extends BaseMapper<Orders, OrdersDto> {
    public OrdersMapper(ModelMapper modelMapper) {
        super(modelMapper, Orders.class);
    }

    public OrdersDto.Response toResponse(Orders entity) {
        return super.toDto(entity, OrdersDto.Response.class);
    }

    public OrdersDto.DetailResponse toDetailResponse(Orders entity,
                                                     String restaurantName,
                                                     String tableNumber,
                                                     String waiterName,
                                                     Integer ticketNumber,
                                                     List<OrdersDto.OrderItemDetail> items) {
        return new OrdersDto.DetailResponse(
                entity.getCode(),
                entity.getRestaurantCode(),
                restaurantName,
                entity.getTableCode(),
                tableNumber,
                entity.getWaiterCode(),
                waiterName,
                entity.getOrderNumber(),
                ticketNumber,
                entity.getOrderType(),
                entity.getStatus(),
                entity.getSubtotal(),
                entity.getDiscountAmount(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.getSpecialNotes(),
                entity.getDeviceType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                items
        );
    }
}
