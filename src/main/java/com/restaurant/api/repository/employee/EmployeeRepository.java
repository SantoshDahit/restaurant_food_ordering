package com.restaurant.api.repository.employee;

import com.restaurant.api.dto.EmployeeDto;
import com.restaurant.api.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findByCode(String code);
    Employee save(Employee employee);
    Page<Employee> search(EmployeeDto.SearchRequest searchRequest, Pageable pageable);
}
