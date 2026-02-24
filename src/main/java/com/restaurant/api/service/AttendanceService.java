package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.AttendanceDto;
import com.restaurant.api.entity.Attendance;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.attendance.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public Attendance getByCode(String code) {
        return attendanceRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.ATTENDANCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Attendance> search(AttendanceDto.SearchRequest searchRequest, Pageable pageable) {
        return attendanceRepository.search(searchRequest, pageable);
    }

    @Transactional
    public Attendance create(AttendanceDto.CreateRequest request) {
        attendanceRepository.findByEmployeeCodeAndAttendanceDate(
                request.employeeCode(), request.attendanceDate()
        ).ifPresent(existing -> {
            throw new ApiException(ErrorCode.ATTENDANCE_ALREADY_EXISTS);
        });
        Attendance attendance = new Attendance(
                UuidUtil.generate(),
                request.employeeCode(),
                request.restaurantCode(),
                request.attendanceDate(),
                request.status(),
                request.checkInTime(),
                request.checkOutTime(),
                request.notes()
        );
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance update(String code, AttendanceDto.PatchRequest request) {
        Attendance attendance = getByCode(code);
        attendance.update(request.status(), request.checkInTime(), request.checkOutTime(),
                request.workedHours(), request.overtimeHours(), request.notes());
        return attendanceRepository.save(attendance);
    }
}
