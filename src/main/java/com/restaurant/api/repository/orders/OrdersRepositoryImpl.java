package com.restaurant.api.repository.orders;

import com.restaurant.api.dto.OrdersDto;
import com.restaurant.api.entity.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrdersRepositoryImpl implements OrdersRepository {
    private final OrdersJpaRepository ordersJpaRepository;
    private final OrdersQueryRepository ordersQueryRepository;

    @Override
    public Optional<Orders> findByCode(String code) {
        return ordersJpaRepository.findByCodeAndDeletedAtIsNull(code);
    }

    @Override
    public Optional<Orders> findByOrderNumber(String orderNumber) {
        return ordersJpaRepository.findByOrderNumberAndDeletedAtIsNull(orderNumber);
    }

    @Override
    public Orders save(Orders orders) {
        return ordersJpaRepository.save(orders);
    }

    @Override
    public Page<Orders> search(OrdersDto.SearchRequest searchRequest, Pageable pageable) {
        return ordersQueryRepository.search(searchRequest, pageable);
    }
}
