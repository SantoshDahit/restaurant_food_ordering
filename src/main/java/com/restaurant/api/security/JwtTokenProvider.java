package com.restaurant.api.security;

import com.restaurant.api.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService customUserDetailsService;

    public String generateAccessToken(String userCode, String role) {
        return buildToken(userCode, role, jwtProperties.getAccessTokenExpiry());
    }

    public String generateRefreshToken(String userCode, String role) {
        return buildToken(userCode, role, jwtProperties.getRefreshTokenExpiry());
    }

    private String buildToken(String userCode, String role, long expiry) {
        Date now = new Date();

        return Jwts.builder()
                .subject(userCode)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Authentication getAuthentication(String token) {
        String userCode = getUserCodeFromToken(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userCode);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BadCredentialsException(ErrorCode.INVALID_TOKEN.getMessage());
        } catch (Exception e) {
            throw new BadCredentialsException(ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    public String getUserCodeFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return (String) getClaims(token).get("role");
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
