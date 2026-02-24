package com.restaurant.api.controller;

import com.restaurant.api.dto.UserDto;
import com.restaurant.api.service.facade.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserFacade userFacade;

    @GetMapping("/{code}")
    public UserDto.Response getByCode(@PathVariable String code) {
        return userFacade.getByCode(code);
    }

    @PatchMapping("/{code}/restaurant")
    public UserDto.Response updateRestaurantCode(@PathVariable String code,
                                                 @RequestParam String restaurantCode) {
        return userFacade.updateRestaurantCode(code, restaurantCode);
    }
}
