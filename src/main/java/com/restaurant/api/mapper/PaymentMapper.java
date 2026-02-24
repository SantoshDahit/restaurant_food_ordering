package com.restaurant.api.mapper;

import com.restaurant.api.dto.PaymentDto;
import com.restaurant.api.entity.Payment;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper extends BaseMapper<Payment, PaymentDto> {
    public PaymentMapper(ModelMapper modelMapper) {
        super(modelMapper, Payment.class);
    }

    public PaymentDto.Response toResponse(Payment entity) {
        return super.toDto(entity, PaymentDto.Response.class);
    }
}
