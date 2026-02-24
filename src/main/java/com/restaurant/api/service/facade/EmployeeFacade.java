package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.EmployeeDto;
import com.restaurant.api.entity.Employee;
import com.restaurant.api.mapper.EmployeeMapper;
import com.restaurant.api.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class EmployeeFacade {
    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;

    @Transactional
    public EmployeeDto.Response create(EmployeeDto.CreateRequest request) {
        Employee employee = employeeService.create(request);
        return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public EmployeeDto.Response getByCode(String code) {
        Employee employee = employeeService.getByCode(code);
        return employeeMapper.toResponse(employee);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto.Response> search(EmployeeDto.SearchRequest request, Pageable pageable) {
        return employeeService.search(request, pageable)
                .map(employeeMapper::toResponse);
    }

    @Transactional
    public EmployeeDto.Response update(String code, EmployeeDto.PatchRequest request) {
        Employee employee = employeeService.update(code, request);
        return employeeMapper.toResponse(employee);
    }

    @Transactional
    public void delete(String code) {
        employeeService.delete(code);
    }
}
