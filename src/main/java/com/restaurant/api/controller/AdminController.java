package com.restaurant.api.controller;

import com.restaurant.api.dto.AdminDto;
import com.restaurant.api.dto.UserDto;
import com.restaurant.api.service.facade.AdminFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminFacade adminFacade;

    @GetMapping("/stats")
    public AdminDto.PlatformStats stats() {
        return adminFacade.stats();
    }

    @GetMapping("/restaurants/{code}/overview")
    public AdminDto.RestaurantOverview restaurantOverview(@PathVariable String code) {
        return adminFacade.restaurantOverview(code);
    }

    @PatchMapping("/users/{code}/role")
    public UserDto.Response changeUserRole(@PathVariable String code,
                                           @Valid @RequestBody AdminDto.RoleChangeRequest request) {
        return adminFacade.changeUserRole(code, request);
    }

    @PatchMapping("/users/{code}/active")
    public UserDto.Response setUserActive(@PathVariable String code,
                                          @Valid @RequestBody AdminDto.ActiveChangeRequest request) {
        return adminFacade.setUserActive(code, request);
    }
}
