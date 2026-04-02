package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Payment getByCode(String code) {
        return paymentRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Payment> search(PaymentDto.SearchRequest searchRequest, Pageable pageable) {
        return paymentRepository.search(searchRequest, pageable);
    }

    @Transactional
    public Payment create(PaymentDto.CreateRequest request) {
        Payment payment = new Payment(
                request.restaurantCode(),
                request.orderCode(),
                request.processedBy(),
                request.paymentMethod(),
                request.amount(),
                request.transactionRef(),
                request.receiptNumber()
        );
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updateStatus(String code, PaymentDto.StatusUpdateRequest request) {
        Payment payment = getByCode(code);
        payment.updateStatus(request.status());
        return paymentRepository.save(payment);
    }
}
