package com.restaurant.api.controller;

import com.restaurant.api.dto.UserDto;
import com.restaurant.api.service.facade.AuthFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthFacade authFacade;

    @PostMapping("/register")
    public UserDto.LoginResponse register(@Valid @RequestBody UserDto.RegisterRequest request) {
        return authFacade.register(request);
    }

    @PostMapping("/login")
    public UserDto.LoginResponse login(@Valid @RequestBody UserDto.LoginRequest request) {
        return authFacade.login(request);
    }
}
