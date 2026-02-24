package com.restaurant.api.entity;

import com.restaurant.api.constant.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @Column(name = "code")
    private String code;

    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "restaurant_code", nullable = false)
    private String restaurantCode;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(name = "check_in_time", length = 10)
    private String checkInTime;

    @Column(name = "check_out_time", length = 10)
    private String checkOutTime;

    @Column(name = "worked_hours", precision = 4, scale = 2)
    private BigDecimal workedHours;

    @Column(name = "overtime_hours", nullable = false, precision = 4, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    public Attendance(String code, String employeeCode, String restaurantCode,
                      LocalDate attendanceDate, AttendanceStatus status,
                      String checkInTime, String checkOutTime, String notes) {
        this.code = code;
        this.employeeCode = employeeCode;
        this.restaurantCode = restaurantCode;
        this.attendanceDate = attendanceDate;
        this.status = status != null ? status : AttendanceStatus.PRESENT;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.notes = notes;
    }

    public void update(AttendanceStatus status, String checkInTime, String checkOutTime,
                       BigDecimal workedHours, BigDecimal overtimeHours, String notes) {
        if (status != null) this.status = status;
        if (checkInTime != null) this.checkInTime = checkInTime;
        if (checkOutTime != null) this.checkOutTime = checkOutTime;
        if (workedHours != null) this.workedHours = workedHours;
        if (overtimeHours != null) this.overtimeHours = overtimeHours;
        if (notes != null) this.notes = notes;
    }
}
