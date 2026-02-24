package com.restaurant.api.repository.orders;

import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrdersRepository {
    Optional<Orders> findByCode(String code);
    Optional<Orders> findByOrderNumber(String orderNumber);
    Orders save(Orders orders);
    Page<Orders> search(OrdersDto.SearchRequest searchRequest, Pageable pageable);
}
