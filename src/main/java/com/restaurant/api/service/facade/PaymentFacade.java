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
    private final OrdersFacade ordersFacade;

    @Transactional
    public PaymentDto.Response create(PaymentDto.CreateRequest request) {
        Payment payment = paymentService.create(request);
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentDto.EsewaInitiateResponse initiateEsewa(PaymentDto.EsewaInitiateRequest request) {
        return paymentService.initiateEsewa(request);
    }

    @Transactional
    public PaymentDto.Response verifyEsewa(PaymentDto.EsewaVerifyRequest request) {
        Payment payment = paymentService.verifyEsewa(request);
        return paymentMapper.toResponse(payment);
    }

    /**
     * Roll back an order whose eSewa payment failed or was cancelled: cancel the
     * order (releasing its ticket) and mark the pending payment FAILED. Guarded
     * so an order that genuinely completed payment is never cancelled.
     */
    @Transactional
    public void cancelUnpaidEsewaOrder(String orderCode) {
        if (paymentService.hasCompletedPayment(orderCode)) {
            return;
        }
        paymentService.failPendingPayments(orderCode);
        ordersFacade.cancelUnpaid(orderCode);
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
