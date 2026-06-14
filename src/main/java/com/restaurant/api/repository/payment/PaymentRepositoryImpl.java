package com.restaurant.api.repository.payment;

import com.restaurant.api.constant.PaymentStatus;
import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public Optional<Payment> findCurrentByOrderCode(String orderCode) {
        // Prefer a settled payment for display; otherwise the most recent attempt.
        return paymentJpaRepository
                .findFirstByOrderCodeAndStatusOrderByCreatedAtDesc(orderCode, PaymentStatus.COMPLETED)
                .or(() -> paymentJpaRepository.findFirstByOrderCodeOrderByCreatedAtDesc(orderCode));
    }

    @Override
    public boolean hasCompletedForOrder(String orderCode) {
        return paymentJpaRepository.existsByOrderCodeAndStatus(orderCode, PaymentStatus.COMPLETED);
    }

    @Override
    public List<Payment> findPendingByOrderCode(String orderCode) {
        return paymentJpaRepository.findByOrderCodeAndStatus(orderCode, PaymentStatus.PENDING);
    }

    @Override
    public List<Payment> findAllWithoutReceipts() {
        return paymentJpaRepository.findAllWithoutReceipts();
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
