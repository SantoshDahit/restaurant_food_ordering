package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.User;
import com.restaurant.api.mapper.UserMapper;
import com.restaurant.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Transactional(readOnly = true)
    public Page<UserDto.Response> search(UserDto.SearchRequest searchRequest, Pageable pageable) {
        return userService.search(searchRequest, pageable).map(userMapper::toResponse);
    }

}
