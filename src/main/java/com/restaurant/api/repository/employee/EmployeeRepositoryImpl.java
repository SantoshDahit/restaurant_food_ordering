package com.restaurant.api.repository.employee;

import com.restaurant.api.dto.EmployeeDto;
import com.restaurant.api.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmployeeRepositoryImpl implements EmployeeRepository {
    private final EmployeeJpaRepository employeeJpaRepository;
    private final EmployeeQueryRepository employeeQueryRepository;

    @Override
    public Optional<Employee> findByCode(String code) {
        return employeeJpaRepository.findByCodeAndDeleteAtIsNull(code);
    }

    @Override
    public Employee save(Employee employee) {
        return employeeJpaRepository.save(employee);
    }

    @Override
    public Page<Employee> search(EmployeeDto.SearchRequest searchRequest, Pageable pageable) {
        return employeeQueryRepository.search(searchRequest, pageable);
    }
}
