package com.restaurant.api.repository.payment;

import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findByCode(String code);
    Optional<Payment> findByOrderCode(String orderCode);
    List<Payment> findAllWithoutReceipts();
    Payment save(Payment payment);
    Page<Payment> search(PaymentDto.SearchRequest searchRequest, Pageable pageable);
}
