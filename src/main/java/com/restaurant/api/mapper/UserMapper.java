package com.restaurant.api.mapper;

import com.restaurant.api.dto.UserDto;
import com.restaurant.api.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends BaseMapper<User, UserDto> {
    public UserMapper(ModelMapper modelMapper) {
        super(modelMapper, User.class);
    }

    public UserDto.Response toResponse(User entity) {
        return super.toDto(entity, UserDto.Response.class);
    }
}
