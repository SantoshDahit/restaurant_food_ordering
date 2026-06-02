package com.restaurant.api.controller;

import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.service.facade.PaymentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentFacade paymentFacade;

    @PostMapping
    public PaymentDto.Response create(@Valid @RequestBody PaymentDto.CreateRequest request) {
        return paymentFacade.create(request);
    }

    /** Start an eSewa payment; returns the signed form fields to POST to eSewa. */
    @PostMapping("/esewa/initiate")
    public PaymentDto.EsewaInitiateResponse initiateEsewa(@Valid @RequestBody PaymentDto.EsewaInitiateRequest request) {
        return paymentFacade.initiateEsewa(request);
    }

    /** Verify eSewa's redirect-back payload and complete the payment. */
    @PostMapping("/esewa/verify")
    public PaymentDto.Response verifyEsewa(@Valid @RequestBody PaymentDto.EsewaVerifyRequest request) {
        return paymentFacade.verifyEsewa(request);
    }

    /** Cancel an order whose eSewa payment failed or was cancelled at the gateway. */
    @PostMapping("/esewa/cancel")
    public void cancelEsewa(@Valid @RequestBody PaymentDto.EsewaCancelRequest request) {
        paymentFacade.cancelUnpaidEsewaOrder(request.orderCode());
    }

    @GetMapping("/{code}")
    public PaymentDto.Response getByCode(@PathVariable String code) {
        return paymentFacade.getByCode(code);
    }

    @GetMapping("/by-order/{orderCode}")
    public PaymentDto.Response getByOrderCode(@PathVariable String orderCode) {
        return paymentFacade.getByOrderCodeOrNull(orderCode);
    }

    @GetMapping("/search")
    public Page<PaymentDto.Response> search(@ModelAttribute PaymentDto.SearchRequest request,
                                            Pageable pageable) {
        return paymentFacade.search(request, pageable);
    }

    @PatchMapping("/{code}/status")
    public PaymentDto.Response updateStatus(@PathVariable String code,
                                            @RequestBody PaymentDto.StatusUpdateRequest request) {
        return paymentFacade.updateStatus(code, request);
    }
}
