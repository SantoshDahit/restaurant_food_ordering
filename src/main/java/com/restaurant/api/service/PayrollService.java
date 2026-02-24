package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.entity.Payroll;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.payroll.PayrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayrollService {
    private final PayrollRepository payrollRepository;

    @Transactional(readOnly = true)
    public Payroll getByCode(String code) {
        return payrollRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYROLL_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Payroll> search(PayrollDto.SearchRequest searchRequest, Pageable pageable) {
        return payrollRepository.search(searchRequest, pageable);
    }

    @Transactional
    public Payroll create(PayrollDto.CreateRequest request) {
        Payroll payroll = new Payroll(
                UuidUtil.generate(),
                request.restaurantCode(),
                request.employeeCode(),
                request.payPeriodStart(),
                request.payPeriodEnd(),
                request.overtimePay(),
                request.bonus(),
                request.deductions(),
                request.netSalary()
        );
        return payrollRepository.save(payroll);
    }

    @Transactional
    public Payroll updateStatus(String code, PayrollDto.StatusUpdateRequest request) {
        Payroll payroll = getByCode(code);
        payroll.updateStatus(request.status());
        return payrollRepository.save(payroll);
    }
}
