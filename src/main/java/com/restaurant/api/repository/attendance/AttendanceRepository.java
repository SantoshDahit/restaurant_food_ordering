package com.restaurant.api.repository.attendance;

import com.restaurant.api.dto.AttendanceDto;
import com.restaurant.api.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository {
    Optional<Attendance> findByCode(String code);
    Optional<Attendance> findByEmployeeCodeAndAttendanceDate(String employeeCode, LocalDate attendanceDate);
    Attendance save(Attendance attendance);
    Page<Attendance> search(AttendanceDto.SearchRequest searchRequest, Pageable pageable);
}
