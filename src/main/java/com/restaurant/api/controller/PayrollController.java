package com.restaurant.api.controller;

import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.service.facade.PayrollFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollFacade payrollFacade;

    @PostMapping
    public PayrollDto.Response create(@Valid @RequestBody PayrollDto.CreateRequest request) {
        return payrollFacade.create(request);
    }

    @GetMapping("/{code}")
    public PayrollDto.Response getByCode(@PathVariable String code) {
        return payrollFacade.getByCode(code);
    }

    @GetMapping("/search")
    public Page<PayrollDto.Response> search(@ModelAttribute PayrollDto.SearchRequest request,
                                            Pageable pageable) {
        return payrollFacade.search(request, pageable);
    }

    @PatchMapping("/{code}/status")
    public PayrollDto.Response updateStatus(@PathVariable String code,
                                            @RequestBody PayrollDto.StatusUpdateRequest request) {
        return payrollFacade.updateStatus(code, request);
    }
}
