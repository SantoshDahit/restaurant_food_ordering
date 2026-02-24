package com.restaurant.api.repository.orders;

import com.restaurant.api.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdersJpaRepository extends JpaRepository<Orders, String> {
    Optional<Orders> findByCodeAndDeleteAtIsNull(String code);
    Optional<Orders> findByOrderNumberAndDeleteAtIsNull(String orderNumber);
    boolean existsByOrderNumberAndDeleteAtIsNull(String orderNumber);
}
