package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.constant.OrderType;
import com.restaurant.api.dto.OrdersDto;
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
     * Handle a failed/cancelled eSewa payment. Always marks the pending payment
     * FAILED. For prepaid channels (kiosk/takeaway) it also rolls back the order
     * (releasing its ticket) — those must be paid before they're cooked. Dine-in
     * (table/QR/waiter) is pay-at-end, so a failed bill payment must NOT cancel
     * the meal the customer already had. Never touches a genuinely paid order.
     */
    @Transactional
    public void cancelUnpaidEsewaOrder(String orderCode) {
        if (paymentService.hasCompletedPayment(orderCode)) {
            return;
        }
        paymentService.failPendingPayments(orderCode);

        OrdersDto.Response order = ordersFacade.getByCode(orderCode);
        if (order.getOrderType() == OrderType.KIOSK || order.getOrderType() == OrderType.TAKEAWAY) {
            ordersFacade.cancelUnpaid(orderCode);
        }
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
