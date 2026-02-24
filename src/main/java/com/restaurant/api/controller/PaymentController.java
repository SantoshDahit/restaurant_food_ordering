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

    @GetMapping("/{code}")
    public PaymentDto.Response getByCode(@PathVariable String code) {
        return paymentFacade.getByCode(code);
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
