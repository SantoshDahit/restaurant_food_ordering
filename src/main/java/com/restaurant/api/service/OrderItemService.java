package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.OrderItemDto;
import com.restaurant.api.entity.MenuItem;
import com.restaurant.api.entity.OrderItem;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.menuitem.MenuItemRepository;
import com.restaurant.api.repository.orderitem.OrderItemRepository;
import com.restaurant.api.repository.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrdersRepository ordersRepository;

    @Transactional(readOnly = true)
    public OrderItem findByCode(String code) {
        return orderItemRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.ORDER_ITEM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findAllByOrderCode(String orderCode) {
        return orderItemRepository.findByOrderCode(orderCode);
    }

    @Transactional
    public OrderItem create(String orderCode, OrderItemDto.CreateRequest request) {
        MenuItem menuItem = menuItemRepository.findByCode(request.menuItemCode())
                .orElseThrow(() -> new ApiException(ErrorCode.MENU_ITEM_NOT_FOUND));
        OrderItem orderItem = new OrderItem(
                orderCode,
                request.menuItemCode(),
                request.quantity(),
                menuItem.getPrice(),
                request.discountAmount(),
                request.spiceLevel(),
                request.notes()
        );
        return orderItemRepository.save(orderItem);
    }

    @Transactional
    public OrderItem update(String code, OrderItemDto.PatchRequest request) {
        OrderItem orderItem = findByCode(code);
        orderItem.update(request.quantity(), request.spiceLevel(), request.notes());
        return orderItemRepository.save(orderItem);
    }

    @Transactional
    public void delete(String code) {
        orderItemRepository.delete(code);
    }
}
