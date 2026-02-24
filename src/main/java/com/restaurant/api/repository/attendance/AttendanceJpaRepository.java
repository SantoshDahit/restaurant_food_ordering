package com.restaurant.api.repository.attendance;

import com.restaurant.api.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceJpaRepository extends JpaRepository<Attendance, String> {
    Optional<Attendance> findByCode(String code);
    Optional<Attendance> findByEmployeeCodeAndAttendanceDate(String employeeCode, LocalDate attendanceDate);
    boolean existsByEmployeeCodeAndAttendanceDate(String employeeCode, LocalDate attendanceDate);
}
