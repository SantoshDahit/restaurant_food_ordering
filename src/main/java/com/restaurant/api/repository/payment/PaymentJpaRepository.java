package com.restaurant.api.repository.payment;

import com.restaurant.api.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByCode(String code);
    Optional<Payment> findByOrderCode(String orderCode);
    boolean existsByOrderCode(String orderCode);
}
