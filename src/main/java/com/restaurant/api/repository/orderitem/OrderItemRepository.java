package com.restaurant.api.repository.orderitem;

import com.restaurant.api.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
    Optional<OrderItem> findByCode(String code);
    List<OrderItem> findByOrderCode(String orderCode);
    OrderItem save(OrderItem orderItem);
    void delete(String code);
}
