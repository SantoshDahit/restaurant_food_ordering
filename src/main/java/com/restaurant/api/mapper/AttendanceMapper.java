package com.restaurant.api.mapper;

import com.restaurant.api.dto.AttendanceDto;
import com.restaurant.api.entity.Attendance;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper extends BaseMapper<Attendance, AttendanceDto> {
    public AttendanceMapper(ModelMapper modelMapper) {
        super(modelMapper, Attendance.class);
    }

    public AttendanceDto.Response toResponse(Attendance entity) {
        return super.toDto(entity, AttendanceDto.Response.class);
    }
}
