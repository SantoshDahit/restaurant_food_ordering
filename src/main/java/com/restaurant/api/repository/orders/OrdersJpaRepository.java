package com.restaurant.api.repository.orders;

import com.restaurant.api.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdersJpaRepository extends JpaRepository<Orders, String> {
    Optional<Orders> findByCodeAndDeletedAtIsNull(String code);
    Optional<Orders> findByOrderNumberAndDeletedAtIsNull(String orderNumber);
    boolean existsByOrderNumberAndDeletedAtIsNull(String orderNumber);
    long countByDeletedAtIsNull();
    long countByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime since);
    long countByRestaurantCodeAndDeletedAtIsNull(String restaurantCode);

    Optional<Orders> findTopByRestaurantCodeAndBusinessDateOrderByTicketNumberDesc(
            String restaurantCode, LocalDate businessDate);

    List<Orders> findAllByTicketNumberIsNullAndDeletedAtIsNull();

    Optional<Orders> findFirstByRestaurantCodeAndDeletedAtIsNullOrderByCreatedAtDesc(String restaurantCode);
}
