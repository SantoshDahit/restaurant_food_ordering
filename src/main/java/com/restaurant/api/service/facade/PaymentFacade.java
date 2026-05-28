package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import com.restaurant.api.mapper.PaymentMapper;
import com.restaurant.api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class PaymentFacade {
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentDto.Response create(PaymentDto.CreateRequest request) {
        Payment payment = paymentService.create(request);
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto.Response getByCode(String code) {
        Payment payment = paymentService.getByCode(code);
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDto.Response getByOrderCodeOrNull(String orderCode) {
        return paymentService.findByOrderCode(orderCode)
                .map(paymentMapper::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto.Response> search(PaymentDto.SearchRequest request, Pageable pageable) {
        return paymentService.search(request, pageable)
                .map(paymentMapper::toResponse);
    }

    @Transactional
    public PaymentDto.Response updateStatus(String code, PaymentDto.StatusUpdateRequest request) {
        Payment payment = paymentService.updateStatus(code, request);
        return paymentMapper.toResponse(payment);
    }
}
