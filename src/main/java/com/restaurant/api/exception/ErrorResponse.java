package com.restaurant.api.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String message;
    private final String errorCode;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public ErrorResponse(ErrorCode errorCode) {
        this.message = errorCode.getMessage();
        this.errorCode = errorCode.name();
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message) {
        this.message = message;
        this.errorCode = "";
        this.timestamp = LocalDateTime.now();
    }
}
