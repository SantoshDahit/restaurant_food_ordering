package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.User;
import com.restaurant.api.mapper.UserMapper;
import com.restaurant.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class UserFacade {
    private final UserService userService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDto.Response getByCode(String code) {
        User user = userService.getByCode(code);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserDto.Response updateRestaurantCode(String code, String restaurantCode) {
        User user = userService.updateRestaurantCode(code, restaurantCode);
        return userMapper.toResponse(user);
    }
}
