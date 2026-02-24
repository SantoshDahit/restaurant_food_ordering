package com.restaurant.api.repository.payroll;

import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.entity.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PayrollRepository {
    Optional<Payroll> findByCode(String code);
    Payroll save(Payroll payroll);
    Page<Payroll> search(PayrollDto.SearchRequest searchRequest, Pageable pageable);
}
