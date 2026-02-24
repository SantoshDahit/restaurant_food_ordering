package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.User;
import com.restaurant.api.mapper.UserMapper;
import com.restaurant.api.security.JwtTokenProvider;
import com.restaurant.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class AuthFacade {
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserDto.LoginResponse register(UserDto.RegisterRequest request) {
        User user = userService.create(request);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getCode(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getCode(), user.getRole().name());
        UserDto.Response userResponse = userMapper.toResponse(user);
        return new UserDto.LoginResponse(accessToken, refreshToken, userResponse);
    }

    @Transactional(readOnly = true)
    public UserDto.LoginResponse login(UserDto.LoginRequest request) {
        User user = userService.authenticate(request.email(), request.password());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getCode(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getCode(), user.getRole().name());
        UserDto.Response userResponse = userMapper.toResponse(user);
        return new UserDto.LoginResponse(accessToken, refreshToken, userResponse);
    }
}
