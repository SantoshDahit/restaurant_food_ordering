package com.restaurant.api.repository.payroll;

import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.entity.Payroll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PayrollRepositoryImpl implements PayrollRepository {
    private final PayrollJpaRepository payrollJpaRepository;
    private final PayrollQueryRepository payrollQueryRepository;

    @Override
    public Optional<Payroll> findByCode(String code) {
        return payrollJpaRepository.findByCodeAndCreatedAtIsNotNull(code);
    }

    @Override
    public Payroll save(Payroll payroll) {
        return payrollJpaRepository.save(payroll);
    }

    @Override
    public Page<Payroll> search(PayrollDto.SearchRequest searchRequest, Pageable pageable) {
        return payrollQueryRepository.search(searchRequest, pageable);
    }
}
