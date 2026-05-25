package com.restaurant.api.security.jwt;

import com.restaurant.api.security.JwtTokenProvider;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * Resolves the bearer token (if present) and places the {@link Authentication}
 * into the {@link SecurityContextHolder}. Never rejects the request itself —
 * access control is enforced by Spring's {@code authorizeHttpRequests} in
 * {@code SecurityConfig}. CORS preflight (OPTIONS) skips token handling.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);
        if (!StringUtils.isBlank(token)) {
            try {
                jwtTokenProvider.validateToken(token);
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                // Invalid token: leave context empty so Spring rejects protected routes with 401.
                log.debug("JWT validation failed: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
