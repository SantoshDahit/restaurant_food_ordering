package com.restaurant.api.repository.attendance;

import com.restaurant.api.dto.AttendanceDto;
import com.restaurant.api.entity.Attendance;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttendanceRepositoryImpl implements AttendanceRepository {
    private final AttendanceJpaRepository attendanceJpaRepository;
    private final AttendanceQueryRepository attendanceQueryRepository;

    @Override
    public Optional<Attendance> findByCode(String code) {
        return attendanceJpaRepository.findByCode(code);
    }

    @Override
    public Optional<Attendance> findByEmployeeCodeAndAttendanceDate(String employeeCode, LocalDate attendanceDate) {
        return attendanceJpaRepository.findByEmployeeCodeAndAttendanceDate(employeeCode, attendanceDate);
    }

    @Override
    public Attendance save(Attendance attendance) {
        return attendanceJpaRepository.save(attendance);
    }

    @Override
    public Page<Attendance> search(AttendanceDto.SearchRequest searchRequest, Pageable pageable) {
        return attendanceQueryRepository.search(searchRequest, pageable);
    }
}
