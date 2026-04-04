package com.restaurant.api.dto;

import com.restaurant.api.constant.SalaryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayrollDto {

    @Schema(description = "Request to create a payroll record")
    public record CreateRequest(
            @Schema(description = "Restaurant code", example = "rest-001")
            @NotBlank String restaurantCode,

            @Schema(description = "Employee code", example = "emp-001")
            @NotBlank String employeeCode,

            @Schema(description = "Pay period start date", example = "2026-03-01")
            @NotNull LocalDate payPeriodStart,

            @Schema(description = "Pay period end date", example = "2026-03-31")
            @NotNull LocalDate payPeriodEnd,

            @Schema(description = "Overtime pay amount", example = "150.00")
            BigDecimal overtimePay,

            @Schema(description = "Bonus amount", example = "200.00")
            BigDecimal bonus,

            @Schema(description = "Deductions amount", example = "50.00")
            BigDecimal deductions,

            @Schema(description = "Net salary amount", example = "3000.00")
            @NotNull BigDecimal netSalary
    ) {}

    @Schema(description = "Request to update payroll status")
    public record StatusUpdateRequest(
            @Schema(description = "New salary status", example = "PAID")
            @NotNull SalaryStatus status
    ) {}

    @Schema(description = "Search filter for payroll records")
    public record SearchRequest(
            @Schema(description = "Filter by restaurant code", example = "rest-001")
            String restaurantCode,

            @Schema(description = "Filter by employee code", example = "emp-001")
            String employeeCode,

            @Schema(description = "Filter by salary status", example = "PENDING")
            SalaryStatus status
    ) {}

    @Getter
    @Schema(description = "Payroll response")
    public static class Response {
        @Schema(description = "Payroll code (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        private String code;

        @Schema(description = "Restaurant code", example = "rest-001")
        private String restaurantCode;

        @Schema(description = "Employee code", example = "emp-001")
        private String employeeCode;

        @Schema(description = "Pay period start date", example = "2026-03-01")
        private LocalDate payPeriodStart;

        @Schema(description = "Pay period end date", example = "2026-03-31")
        private LocalDate payPeriodEnd;

        @Schema(description = "Overtime pay amount", example = "150.00")
        private BigDecimal overtimePay;

        @Schema(description = "Bonus amount", example = "200.00")
        private BigDecimal bonus;

        @Schema(description = "Deductions amount", example = "50.00")
        private BigDecimal deductions;

        @Schema(description = "Net salary amount", example = "3000.00")
        private BigDecimal netSalary;

        @Schema(description = "Salary status", example = "PENDING")
        private SalaryStatus status;

        @Schema(description = "Date and time when salary was paid")
        private LocalDateTime paidAt;

        @Schema(description = "Record creation timestamp")
        private LocalDateTime createdAt;

        @Schema(description = "Record last update timestamp")
        private LocalDateTime updatedAt;
    }
}
