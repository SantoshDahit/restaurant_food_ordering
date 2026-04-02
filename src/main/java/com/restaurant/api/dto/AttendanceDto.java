package com.restaurant.api.dto;

import com.restaurant.api.constant.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AttendanceDto {

    public record CreateRequest(
            @NotBlank String employeeCode,
            @NotBlank String restaurantCode,
            @NotNull LocalDate attendanceDate,
            AttendanceStatus status,
            String checkInTime,
            String checkOutTime,
            String notes
    ) {}

    public record PatchRequest(
            AttendanceStatus status,
            String checkInTime,
            String checkOutTime,
            BigDecimal workedHours,
            BigDecimal overtimeHours,
            String notes
    ) {}

    public record SearchRequest(
            String restaurantCode,
            String employeeCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            AttendanceStatus status
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String employeeCode;
        private String restaurantCode;
        private LocalDate attendanceDate;
        private AttendanceStatus status;
        private String checkInTime;
        private String checkOutTime;
        private BigDecimal workedHours;
        private BigDecimal overtimeHours;
        private String notes;
        private LocalDateTime createdAt;
    }
}
