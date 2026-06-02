package com.restaurant.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
    DUPLICATE_ENTRY(HttpStatus.CONFLICT, "Duplicate entry"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "Data integrity violation"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    MISSING_REQUEST_BODY(HttpStatus.BAD_REQUEST, "Request body is missing"),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid or expired token"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User with this email already exists"),

    // Restaurant
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "Restaurant not found"),
    RESTAURANT_ALREADY_EXISTS_FOR_USER(HttpStatus.CONFLICT, "User already owns a restaurant"),

    // Table
    TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Table not found"),
    TABLE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Table number already exists in this restaurant"),

    // Menu Category
    MENU_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Menu category not found"),

    // Menu Item
    MENU_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Menu item not found"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
    ORDER_CANNOT_BE_MODIFIED(HttpStatus.BAD_REQUEST, "Order cannot be modified in current status"),
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "Invalid order status transition"),
    TABLE_HAS_ACTIVE_ORDER(HttpStatus.CONFLICT, "This table already has an active order. Please ask staff for help."),

    // Order Item
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Order item not found"),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment not found"),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "Order has already been paid"),
    PAYMENT_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "Payment could not be verified with the gateway"),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "Paid amount does not match the order amount"),
    PAYMENT_GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "Payment gateway is unavailable"),

    // Receipt
    RECEIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "Receipt not found"),

    // Employee
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "Employee not found"),

    // Attendance
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Attendance record not found"),
    ATTENDANCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Attendance record already exists for this date"),

    // Payroll
    PAYROLL_NOT_FOUND(HttpStatus.NOT_FOUND, "Payroll record not found"),

    // File
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "File not found"),

    // Email verification
    EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Email verification record not found"),
    EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "Email verification code has expired"),
    EMAIL_VERIFICATION_PIN_MISMATCH(HttpStatus.BAD_REQUEST, "Email verification PIN does not match"),
    EMAIL_VERIFICATION_EMAIL_MISMATCH(HttpStatus.BAD_REQUEST, "Email does not match the verification record"),
    EMAIL_VERIFICATION_PURPOSE_MISMATCH(HttpStatus.BAD_REQUEST, "Email verification purpose does not match"),
    EMAIL_VERIFICATION_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "Email has not been verified"),
    EMAIL_VERIFICATION_ALREADY_USED(HttpStatus.BAD_REQUEST, "Email verification has already been used"),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
