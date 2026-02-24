package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.OrderItemDto;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.mapper.OrderItemMapper;
import com.restaurant.api.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Facade
@RequiredArgsConstructor
public class OrderItemFacade {
    private final OrderItemService orderItemService;
    private final OrderItemMapper orderItemMapper;

    @Transactional
    public OrderItemDto.Response addItem(String orderCode, OrderItemDto.CreateRequest request) {
        OrderItem orderItem = orderItemService.create(orderCode, request);
        return orderItemMapper.toResponse(orderItem);
    }

    @Transactional
    public OrderItemDto.Response updateItem(String orderCode, String itemCode, OrderItemDto.PatchRequest request) {
        OrderItem orderItem = orderItemService.update(itemCode, request);
        return orderItemMapper.toResponse(orderItem);
    }

    @Transactional
    public void removeItem(String orderCode, String itemCode) {
        orderItemService.delete(itemCode);
    }

    @Transactional(readOnly = true)
    public List<OrderItemDto.Response> getItemsByOrder(String orderCode) {
        return orderItemService.findAllByOrderCode(orderCode)
                .stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}
