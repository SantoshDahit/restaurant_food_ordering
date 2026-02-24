package com.restaurant.api.mapper;

import com.restaurant.api.dto.PayrollDto;
import com.restaurant.api.entity.Payroll;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper extends BaseMapper<Payroll, PayrollDto> {
    public PayrollMapper(ModelMapper modelMapper) {
        super(modelMapper, Payroll.class);
    }

    public PayrollDto.Response toResponse(Payroll entity) {
        return super.toDto(entity, PayrollDto.Response.class);
    }
}
