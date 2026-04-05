package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.mapper.OrdersMapper;
import com.restaurant.api.service.OrderItemService;
import com.restaurant.api.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class OrdersFacade {
    private final OrdersService ordersService;
    private final OrderItemService orderItemService;
    private final OrdersMapper ordersMapper;

    @Transactional
    public OrdersDto.Response create(OrdersDto.CreateRequest request) {
        Orders orders = ordersService.create(request);
        return ordersMapper.toResponse(orders);
    }

    @Transactional(readOnly = true)
    public OrdersDto.Response getByCode(String code) {
        Orders orders = ordersService.getByCode(code);
        return ordersMapper.toResponse(orders);
    }

    @Transactional(readOnly = true)
    public Page<OrdersDto.Response> search(OrdersDto.SearchRequest request, Pageable pageable) {
        return ordersService.search(request, pageable)
                .map(ordersMapper::toResponse);
    }

    @Transactional
    public OrdersDto.Response updateStatus(String code, OrdersDto.StatusUpdateRequest request) {
        Orders orders = ordersService.updateStatus(code, request);
        orderItemService.updateStatusByOrderCode(code, request.status());
        return ordersMapper.toResponse(orders);
    }

    @Transactional
    public void delete(String code) {
        ordersService.delete(code);
    }
}
