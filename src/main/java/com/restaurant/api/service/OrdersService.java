package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.orderitem.OrderItemRepository;
import com.restaurant.api.repository.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Orders getByCode(String code) {
        return ordersRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Orders> search(OrdersDto.SearchRequest searchRequest, Pageable pageable) {
        return ordersRepository.search(searchRequest, pageable);
    }

    @Transactional
    public Orders create(OrdersDto.CreateRequest request) {
        String orderNumber = "ORD-" + System.currentTimeMillis();
        Orders orders = new Orders(
                request.restaurantCode(),
                request.tableCode(),
                request.waiterCode(),
                orderNumber,
                request.orderType(),
                request.specialNotes(),
                request.deviceType()
        );
        return ordersRepository.save(orders);
    }

    @Transactional
    public Orders updateStatus(String code, OrdersDto.StatusUpdateRequest request) {
        Orders orders = getByCode(code);
        orders.updateStatus(request.status());
        return ordersRepository.save(orders);
    }

    @Transactional
    public void delete(String code) {
        Orders orders = getByCode(code);
        orders.cancel();
        ordersRepository.save(orders);
    }

    @Transactional
    public Orders recalculateTotals(String orderCode, List<OrderItem> items) {
        Orders orders = getByCode(orderCode);
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orders.updateAmounts(subtotal, orders.getDiscountAmount(), orders.getTaxAmount(), subtotal);
        return ordersRepository.save(orders);
    }
}
