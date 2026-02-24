package com.restaurant.api.repository.orderitem;

import com.restaurant.api.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final OrderItemQueryRepository orderItemQueryRepository;

    @Override
    public Optional<OrderItem> findByCode(String code) {
        return orderItemJpaRepository.findByCodeAndOrderCode(code, null)
                .or(() -> orderItemJpaRepository.findById(code));
    }

    @Override
    public List<OrderItem> findByOrderCode(String orderCode) {
        return orderItemQueryRepository.findAllByOrderCode(orderCode);
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemJpaRepository.save(orderItem);
    }

    @Override
    public void delete(String code) {
        orderItemJpaRepository.deleteByCode(code);
    }
}
