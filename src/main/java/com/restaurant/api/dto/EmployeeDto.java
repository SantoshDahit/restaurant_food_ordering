package com.restaurant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeDto {

    public record CreateRequest(
            @NotBlank String restaurantCode,
            @NotBlank String fullName,
            String phone,
            @NotNull LocalDate joinDate,
            @NotNull BigDecimal baseSalary,
            String bankAccount,
            String bankName,
            String fileCode
    ) {}

    public record PatchRequest(
            String fullName,
            String phone,
            BigDecimal baseSalary,
            String bankAccount,
            String bankName,
            String fileCode
    ) {}

    public record SearchRequest(
            String restaurantCode,
            String fullName,
            Boolean isActive
    ) {}

    @Getter
    public static class Response {
        private String code;
        private String restaurantCode;
        private String fileCode;
        private String fullName;
        private String phone;
        private LocalDate joinDate;
        private BigDecimal baseSalary;
        private String bankAccount;
        private String bankName;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
