package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.OrderItemDto;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.mapper.OrderItemMapper;
import com.restaurant.api.service.OrderItemService;
import com.restaurant.api.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Facade
@RequiredArgsConstructor
public class OrderItemFacade {
    private final OrderItemService orderItemService;
    private final OrderItemMapper orderItemMapper;
    private final OrdersService ordersService;

    @Transactional
    public OrderItemDto.Response addItem(String orderCode, OrderItemDto.CreateRequest request) {
        OrderItem orderItem = orderItemService.create(orderCode, request);
        recalculate(orderCode);
        return orderItemMapper.toResponse(orderItem);
    }

    @Transactional
    public OrderItemDto.Response updateItem(String orderCode, String itemCode, OrderItemDto.PatchRequest request) {
        OrderItem orderItem = orderItemService.update(itemCode, request);
        recalculate(orderCode);
        return orderItemMapper.toResponse(orderItem);
    }

    @Transactional
    public void removeItem(String orderCode, String itemCode) {
        orderItemService.delete(itemCode);
        recalculate(orderCode);
    }

    private void recalculate(String orderCode) {
        List<OrderItem> items = orderItemService.findAllByOrderCode(orderCode);
        ordersService.recalculateTotals(orderCode, items);
    }

    @Transactional(readOnly = true)
    public List<OrderItemDto.Response> getItemsByOrder(String orderCode) {
        return orderItemService.findAllByOrderCode(orderCode)
                .stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}
