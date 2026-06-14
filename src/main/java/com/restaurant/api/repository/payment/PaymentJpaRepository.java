package com.restaurant.api.repository.payment;

import com.restaurant.api.constant.PaymentStatus;
import com.restaurant.api.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByCode(String code);
    boolean existsByOrderCode(String orderCode);

    // An order can have several payment rows (retries / multiple attempts), so
    // any per-order lookup must tolerate >1 row — never a single-result query.
    boolean existsByOrderCodeAndStatus(String orderCode, PaymentStatus status);
    List<Payment> findByOrderCodeAndStatus(String orderCode, PaymentStatus status);
    Optional<Payment> findFirstByOrderCodeAndStatusOrderByCreatedAtDesc(String orderCode, PaymentStatus status);
    Optional<Payment> findFirstByOrderCodeOrderByCreatedAtDesc(String orderCode);

    @Query("""
        SELECT p FROM Payment p
        WHERE NOT EXISTS (
            SELECT 1 FROM com.restaurant.api.entity.Receipt r
            WHERE r.orderCode = p.orderCode AND r.deletedAt IS NULL
        )
        ORDER BY p.createdAt ASC
    """)
    List<Payment> findAllWithoutReceipts();

    @Query("SELECT COALESCE(SUM(p.amount - p.refundedAmount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumNetAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount - p.refundedAmount), 0) FROM Payment p WHERE p.status = :status AND p.processedAt >= :since")
    BigDecimal sumNetAmountByStatusSince(@Param("status") PaymentStatus status, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(p.amount - p.refundedAmount), 0) FROM Payment p WHERE p.status = :status AND p.restaurantCode = :restaurantCode")
    BigDecimal sumNetAmountByStatusAndRestaurant(@Param("status") PaymentStatus status, @Param("restaurantCode") String restaurantCode);
}
