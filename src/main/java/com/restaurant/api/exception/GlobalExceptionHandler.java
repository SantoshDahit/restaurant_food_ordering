package com.restaurant.api.exception;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException e) {
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode());
        return new ResponseEntity<>(errorResponse, e.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParams(MissingServletRequestParameterException ex) {
        log.error(ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                "Required request parameter '" + ex.getParameterName() + "' is missing."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage(), ex);

        Throwable cause = ex.getCause();
        if (cause instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
            String fieldName = unrecognizedPropertyException.getPropertyName();
            ErrorResponse errorResponse = new ErrorResponse(
                    "Unsupported field: " + fieldName
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse errorResponse = new ErrorResponse("JSON parsing error");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        if (Objects.nonNull(requestUri) && !requestUri.endsWith("/favicon.ico")) {
            log.error(ex.getMessage(), ex);
        }

        ErrorResponse errorResponse = new ErrorResponse(
                "Resource " + ex.getResourcePath() + " not found."
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> internalServerError(Exception e, HttpServletRequest request) {
        log.error(e.getMessage(), e);

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
