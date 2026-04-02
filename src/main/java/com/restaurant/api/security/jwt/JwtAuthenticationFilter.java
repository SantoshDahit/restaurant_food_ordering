package com.restaurant.api.security.jwt;

import com.restaurant.api.security.JwtTokenProvider;
import com.restaurant.api.security.config.SecurityProperties;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final SecurityProperties securityProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();

        String token = jwtTokenProvider.resolveToken(request);

        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        List<String> whitelist = securityProperties.getWhitelist();
        if (whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
            if (!StringUtils.isBlank(token)) {
                try {
                    jwtTokenProvider.validateToken(token);
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception ignored) {}
            }
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (StringUtils.isBlank(token)) {
            jwtAuthenticationEntryPoint.commence(
                    request, (HttpServletResponse) servletResponse, new InsufficientAuthenticationException("")
            );
            return;
        }

        try {
            jwtTokenProvider.validateToken(token);
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception ex) {
            log.error("JWT error: {}", ex.getMessage());
            jwtAuthenticationEntryPoint.commence(
                    request, (HttpServletResponse) servletResponse, new InsufficientAuthenticationException("")
            );
        }
    }
}
