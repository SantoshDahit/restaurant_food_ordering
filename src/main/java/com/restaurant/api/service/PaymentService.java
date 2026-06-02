package com.restaurant.api.service;

import com.restaurant.api.constant.PaymentMethod;
import com.restaurant.api.constant.PaymentStatus;
import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Orders;
import com.restaurant.api.entity.Payment;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.gateway.esewa.EsewaFormData;
import com.restaurant.api.gateway.esewa.EsewaGateway;
import com.restaurant.api.gateway.esewa.EsewaVerification;
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
    private final OrdersService ordersService;
    private final ReceiptService receiptService;
    private final EsewaGateway esewaGateway;

    @Transactional(readOnly = true)
    public Payment getByCode(String code) {
        return paymentRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Payment> findByOrderCode(String orderCode) {
        return paymentRepository.findByOrderCode(orderCode);
    }

    @Transactional(readOnly = true)
    public Page<Payment> search(PaymentDto.SearchRequest searchRequest, Pageable pageable) {
        return paymentRepository.search(searchRequest, pageable);
    }

    @Transactional
    public Payment create(PaymentDto.CreateRequest request) {
        // Verify the order exists before insert so we return a clean 404
        // instead of a DB FK violation 500.
        Orders order = ordersService.getByCode(request.orderCode());

        Payment payment = new Payment(
                request.restaurantCode(),
                request.orderCode(),
                request.processedBy(),
                request.paymentMethod(),
                request.amount(),
                request.transactionRef(),
                request.receiptNumber()
        );
        // Digital methods (eSewa, Khalti, PhonePay, iBank) confirm at the gateway
        // so we mark them COMPLETED immediately. CASH and POS stay PENDING — the
        // cashier confirms manually after physically receiving the payment.
        if (isAutoCompleteMethod(request.paymentMethod())) {
            payment.complete();
        }
        Payment saved = paymentRepository.save(payment);
        // Issue the immutable receipt now. Idempotent — won't double-issue if retried.
        receiptService.issue(saved, order);
        return saved;
    }

    private boolean isAutoCompleteMethod(PaymentMethod m) {
        // CASH/POS settle manually at the counter. ESEWA settles through the
        // real gateway (PENDING until the verified callback). The remaining
        // digital methods are still simulated and confirm on creation.
        return m != null
                && m != PaymentMethod.CASH
                && m != PaymentMethod.POS
                && m != PaymentMethod.ESEWA;
    }

    /**
     * Create a PENDING eSewa payment and hand back the signed form fields the
     * browser submits to eSewa. The payment is completed only once the signed
     * callback is verified in {@link #verifyEsewa}.
     */
    @Transactional
    public PaymentDto.EsewaInitiateResponse initiateEsewa(PaymentDto.EsewaInitiateRequest request) {
        ordersService.getByCode(request.orderCode()); // 404 early if the order is bogus

        Payment payment = new Payment(
                request.restaurantCode(),
                request.orderCode(),
                null,
                PaymentMethod.ESEWA,
                request.amount(),
                null,
                null
        );
        Payment saved = paymentRepository.save(payment);

        EsewaFormData form = esewaGateway.initiate(
                saved.getCode(), saved.getAmount(), request.successUrl(), request.failureUrl());
        return new PaymentDto.EsewaInitiateResponse(saved.getCode(), form.formUrl(), form.fields());
    }

    /**
     * Verify an eSewa redirect-back payload and complete the payment.
     * Idempotent: a payment already COMPLETED is returned untouched so a
     * double callback (or a page refresh) is harmless.
     */
    @Transactional
    public Payment verifyEsewa(PaymentDto.EsewaVerifyRequest request) {
        EsewaVerification verification = esewaGateway.verify(request.data());

        Payment payment = paymentRepository.findByCode(verification.transactionUuid())
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return payment;
        }
        if (!verification.success()) {
            payment.fail();
            return paymentRepository.save(payment);
        }
        if (payment.getAmount().compareTo(verification.totalAmount()) != 0) {
            throw new ApiException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        payment.completeWithRef(verification.transactionCode());
        Payment saved = paymentRepository.save(payment);

        Orders order = ordersService.getByCode(saved.getOrderCode());
        receiptService.issue(saved, order);
        return saved;
    }

    @Transactional(readOnly = true)
    public boolean hasCompletedPayment(String orderCode) {
        return paymentRepository.findByOrderCode(orderCode)
                .map(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .orElse(false);
    }

    /** Mark a still-pending payment for this order as FAILED (e.g. gateway cancel). */
    @Transactional
    public void failPendingPayments(String orderCode) {
        paymentRepository.findByOrderCode(orderCode).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.PENDING) {
                p.fail();
                paymentRepository.save(p);
            }
        });
    }

    @Transactional
    public Payment updateStatus(String code, PaymentDto.StatusUpdateRequest request) {
        Payment payment = getByCode(code);
        payment.updateStatus(request.status());
        return paymentRepository.save(payment);
    }
}
