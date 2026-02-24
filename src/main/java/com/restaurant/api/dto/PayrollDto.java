package com.restaurant.api.dto;

import com.restaurant.api.constant.SalaryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayrollDto {

    public record CreateRequest(
            @NotBlank String restaurantCode,
            @NotBlank String employeeCode,
            @NotNull LocalDate payPeriodStart,
            @NotNull LocalDate payPeriodEnd,
            BigDecimal overtimePay,
            BigDecimal bonus,
            BigDecimal deductions,
            @NotNull BigDecimal netSalary
    ) {}

    public record StatusUpdateRequest(
            @NotNull SalaryStatus status
    ) {}

    public record SearchRequest(
            String restaurantCode,
            String employeeCode,
            SalaryStatus status
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String restaurantCode;
        private String employeeCode;
        private LocalDate payPeriodStart;
        private LocalDate payPeriodEnd;
        private BigDecimal overtimePay;
        private BigDecimal bonus;
        private BigDecimal deductions;
        private BigDecimal netSalary;
        private SalaryStatus status;
        private LocalDateTime paidAt;
        private LocalDateTime createAt;
        private LocalDateTime updateAt;
    }
}
