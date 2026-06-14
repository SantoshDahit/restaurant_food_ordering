package com.restaurant.api.repository.payment;

import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findByCode(String code);
    /** The payment to show for an order: the latest COMPLETED one, else the latest of any status. */
    Optional<Payment> findCurrentByOrderCode(String orderCode);
    /** True if the order has any settled payment. */
    boolean hasCompletedForOrder(String orderCode);
    /** All still-pending payment rows for an order (an order may have several attempts). */
    List<Payment> findPendingByOrderCode(String orderCode);
    List<Payment> findAllWithoutReceipts();
    Payment save(Payment payment);
    Page<Payment> search(PaymentDto.SearchRequest searchRequest, Pageable pageable);
}
