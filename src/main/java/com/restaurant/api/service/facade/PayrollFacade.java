package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.entity.Payroll;
import com.restaurant.api.mapper.PayrollMapper;
import com.restaurant.api.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class PayrollFacade {
    private final PayrollService payrollService;
    private final PayrollMapper payrollMapper;

    @Transactional
    public PayrollDto.Response create(PayrollDto.CreateRequest request) {
        Payroll payroll = payrollService.create(request);
        return payrollMapper.toResponse(payroll);
    }

    @Transactional(readOnly = true)
    public PayrollDto.Response getByCode(String code) {
        Payroll payroll = payrollService.getByCode(code);
        return payrollMapper.toResponse(payroll);
    }

    @Transactional(readOnly = true)
    public Page<PayrollDto.Response> search(PayrollDto.SearchRequest request, Pageable pageable) {
        return payrollService.search(request, pageable)
                .map(payrollMapper::toResponse);
    }

    @Transactional
    public PayrollDto.Response updateStatus(String code, PayrollDto.StatusUpdateRequest request) {
        Payroll payroll = payrollService.updateStatus(code, request);
        return payrollMapper.toResponse(payroll);
    }
}
