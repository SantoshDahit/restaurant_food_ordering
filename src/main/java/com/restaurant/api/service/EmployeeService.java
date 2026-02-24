package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.EmployeeDto;
import com.restaurant.api.entity.Employee;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public Employee getByCode(String code) {
        return employeeRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.EMPLOYEE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Employee> search(EmployeeDto.SearchRequest searchRequest, Pageable pageable) {
        return employeeRepository.search(searchRequest, pageable);
    }

    @Transactional
    public Employee create(EmployeeDto.CreateRequest request) {
        Employee employee = new Employee(
                UuidUtil.generate(),
                request.restaurantCode(),
                request.fullName(),
                request.phone(),
                request.joinDate(),
                request.baseSalary(),
                request.bankAccount(),
                request.bankName()
        );
        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(String code, EmployeeDto.PatchRequest request) {
        Employee employee = getByCode(code);
        employee.update(request.fullName(), request.phone(), request.baseSalary(),
                request.bankAccount(), request.bankName(), request.fileCode());
        return employeeRepository.save(employee);
    }

    @Transactional
    public void delete(String code) {
        Employee employee = getByCode(code);
        employee.deactivate();
        employeeRepository.save(employee);
    }
}
