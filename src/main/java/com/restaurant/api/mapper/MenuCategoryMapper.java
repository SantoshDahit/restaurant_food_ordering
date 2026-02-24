package com.restaurant.api.mapper;

import com.restaurant.api.dto.MenuCategoryDto;
import com.restaurant.api.entity.MenuCategory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class MenuCategoryMapper extends BaseMapper<MenuCategory, MenuCategoryDto> {
    public MenuCategoryMapper(ModelMapper modelMapper) {
        super(modelMapper, MenuCategory.class);
    }

    public MenuCategoryDto.Response toResponse(MenuCategory entity) {
        return super.toDto(entity, MenuCategoryDto.Response.class);
    }
}
