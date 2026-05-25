package com.restaurant.api.config;

import com.restaurant.api.security.handler.CustomAccessDeniedHandler;
import com.restaurant.api.security.jwt.JwtAuthenticationEntryPoint;
import com.restaurant.api.security.jwt.JwtAuthenticationFilter;
import com.restaurant.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth + docs (always public)
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers("/v1/email-verifications/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Public lookups the customer-facing UI relies on
                        .requestMatchers(HttpMethod.GET, "/v1/restaurants/by-kiosk-code/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/tables/by-token/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/menu-categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/menu-items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/files/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                        // Customer order flow (QR/kiosk: place an order, add items, view status, pay)
                        .requestMatchers(HttpMethod.POST, "/v1/orders").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/orders/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/orders/*/detail").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/orders/*/items").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/orders/*/items").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/payments").permitAll()

                        // Platform admin endpoints — ADMIN role only
                        .requestMatchers("/v1/admin/**").hasAuthority("ADMIN")

                        // Everything else requires a valid JWT
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}
