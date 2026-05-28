package com.restaurant.api.mapper;

import com.restaurant.api.dto.ReceiptDto;
import com.restaurant.api.entity.Receipt;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ReceiptMapper extends BaseMapper<Receipt, ReceiptDto> {
    public ReceiptMapper(ModelMapper modelMapper) {
        super(modelMapper, Receipt.class);
    }

    public ReceiptDto.Response toResponse(Receipt entity) {
        return super.toDto(entity, ReceiptDto.Response.class);
    }
}
