package com.restaurant.api.mapper;

import com.restaurant.api.dto.EmailVerificationDto;
import com.restaurant.api.entity.EmailVerification;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationMapper extends BaseMapper<EmailVerification, EmailVerificationDto> {
    public EmailVerificationMapper(ModelMapper modelMapper) {
        super(modelMapper, EmailVerification.class);
    }

    public EmailVerificationDto.Response toResponse(EmailVerification entity) {
        return super.toDto(entity, EmailVerificationDto.Response.class);
    }
}
