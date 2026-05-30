package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Orders;
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
    private final OrdersService ordersService;
    private final ReceiptService receiptService;

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

    private boolean isAutoCompleteMethod(com.restaurant.api.constant.PaymentMethod m) {
        return m != null
                && m != com.restaurant.api.constant.PaymentMethod.CASH
                && m != com.restaurant.api.constant.PaymentMethod.POS;
    }

    @Transactional
    public Payment updateStatus(String code, PaymentDto.StatusUpdateRequest request) {
        Payment payment = getByCode(code);
        payment.updateStatus(request.status());
        return paymentRepository.save(payment);
    }
}
