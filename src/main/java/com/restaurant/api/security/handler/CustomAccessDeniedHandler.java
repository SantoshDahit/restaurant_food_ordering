package com.restaurant.api.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUrl = uri + (query != null ? "?" + query : "");

        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            headers.append(name).append(":\"").append(value).append("\", ");
        }
        if (headers.length() > 2) {
            headers.setLength(headers.length() - 2);
        }

        log.warn("Access Denied: {} {}, headers=[{}]", method, fullUrl, headers);

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.ACCESS_DENIED);

        String res = objectMapper.writeValueAsString(errorResponse);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(res);
    }
}
