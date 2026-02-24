package com.restaurant.api.mapper;

import com.restaurant.api.dto.EmployeeDto;
import com.restaurant.api.entity.Employee;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper extends BaseMapper<Employee, EmployeeDto> {
    public EmployeeMapper(ModelMapper modelMapper) {
        super(modelMapper, Employee.class);
    }

    public EmployeeDto.Response toResponse(Employee entity) {
        return super.toDto(entity, EmployeeDto.Response.class);
    }
}
