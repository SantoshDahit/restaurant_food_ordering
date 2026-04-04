package com.restaurant.api.repository.payroll;

import com.restaurant.api.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PayrollJpaRepository extends JpaRepository<Payroll, String> {
    Optional<Payroll> findByCode(String code);
    Optional<Payroll> findByCodeAndCreatedAtIsNotNull(String code);
    boolean existsByEmployeeCodeAndPayPeriodStartAndPayPeriodEnd(
            String employeeCode, LocalDate payPeriodStart, LocalDate payPeriodEnd);
}
