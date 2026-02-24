package com.restaurant.api.repository.payment;

import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentQueryRepository paymentQueryRepository;

    @Override
    public Optional<Payment> findByCode(String code) {
        return paymentJpaRepository.findByCode(code);
    }

    @Override
    public Optional<Payment> findByOrderCode(String orderCode) {
        return paymentJpaRepository.findByOrderCode(orderCode);
    }

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Page<Payment> search(PaymentDto.SearchRequest searchRequest, Pageable pageable) {
        return paymentQueryRepository.search(searchRequest, pageable);
    }
}
