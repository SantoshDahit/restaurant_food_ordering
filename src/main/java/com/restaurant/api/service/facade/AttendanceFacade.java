package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.AttendanceDto;
import com.restaurant.api.entity.Attendance;
import com.restaurant.api.mapper.AttendanceMapper;
import com.restaurant.api.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class AttendanceFacade {
    private final AttendanceService attendanceService;
    private final AttendanceMapper attendanceMapper;

    @Transactional
    public AttendanceDto.Response create(AttendanceDto.CreateRequest request) {
        Attendance attendance = attendanceService.create(request);
        return attendanceMapper.toResponse(attendance);
    }

    @Transactional(readOnly = true)
    public AttendanceDto.Response getByCode(String code) {
        Attendance attendance = attendanceService.getByCode(code);
        return attendanceMapper.toResponse(attendance);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto.Response> search(AttendanceDto.SearchRequest request, Pageable pageable) {
        return attendanceService.search(request, pageable)
                .map(attendanceMapper::toResponse);
    }

    @Transactional
    public AttendanceDto.Response update(String code, AttendanceDto.PatchRequest request) {
        Attendance attendance = attendanceService.update(code, request);
        return attendanceMapper.toResponse(attendance);
    }
}
