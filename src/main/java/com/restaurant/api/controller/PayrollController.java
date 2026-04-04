package com.restaurant.api.controller;

import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.service.facade.PayrollFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Payroll management APIs")
public class PayrollController {
    private final PayrollFacade payrollFacade;

    @PostMapping
    @Operation(summary = "Create payroll", description = "Create a new payroll record for an employee")
    public PayrollDto.Response create(@Valid @RequestBody PayrollDto.CreateRequest request) {
        return payrollFacade.create(request);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get payroll by code", description = "Retrieve a payroll record by its unique code")
    public PayrollDto.Response getByCode(
            @Parameter(description = "Payroll code (UUID)") @PathVariable String code) {
        return payrollFacade.getByCode(code);
    }

    @GetMapping("/search")
    @Operation(summary = "Search payroll", description = "Search payroll records with filters and pagination")
    public Page<PayrollDto.Response> search(@ModelAttribute PayrollDto.SearchRequest request,
                                            Pageable pageable) {
        return payrollFacade.search(request, pageable);
    }

    @PatchMapping("/{code}/status")
    @Operation(summary = "Update payroll status", description = "Update the status of a payroll record")
    public PayrollDto.Response updateStatus(
            @Parameter(description = "Payroll code (UUID)") @PathVariable String code,
            @RequestBody PayrollDto.StatusUpdateRequest request) {
        return payrollFacade.updateStatus(code, request);
    }
}
