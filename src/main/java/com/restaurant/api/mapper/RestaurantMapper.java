package com.restaurant.api.mapper;

import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.Restaurant;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper extends BaseMapper<Restaurant, RestaurantDto> {
    public RestaurantMapper(ModelMapper modelMapper) {
        super(modelMapper, Restaurant.class);
    }

    public RestaurantDto.Response toResponse(Restaurant entity) {
        return super.toDto(entity, RestaurantDto.Response.class);
    }
}
