package com.restaurant.api.repository.orderitem;

import com.restaurant.api.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, String> {
    Optional<OrderItem> findByCodeAndOrderCode(String code, String orderCode);
    List<OrderItem> findAllByOrderCode(String orderCode);

    @Modifying
    @Query("DELETE FROM OrderItem oi WHERE oi.code = :code")
    void deleteByCode(@Param("code") String code);
}
