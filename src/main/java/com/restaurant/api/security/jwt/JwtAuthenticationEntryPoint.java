package com.restaurant.api.security.jwt;

import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MappingJackson2HttpMessageConverter messageConverter;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_TOKEN);
        ServletServerHttpResponse outputMessage = new ServletServerHttpResponse(response);
        messageConverter.write(errorResponse, MediaType.APPLICATION_JSON, outputMessage);
    }
}
