package com.restaurant.api.mapper;

import com.restaurant.api.dto.MenuItemDto;
import com.restaurant.api.entity.MenuItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper extends BaseMapper<MenuItem, MenuItemDto> {
    public MenuItemMapper(ModelMapper modelMapper) {
        super(modelMapper, MenuItem.class);
    }

    public MenuItemDto.Response toResponse(MenuItem entity) {
        return super.toDto(entity, MenuItemDto.Response.class);
    }
}
