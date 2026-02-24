package com.restaurant.api.mapper;

import com.restaurant.api.dto.RestaurantTableDto;
import com.restaurant.api.entity.RestaurantTable;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class RestaurantTableMapper extends BaseMapper<RestaurantTable, RestaurantTableDto> {
    public RestaurantTableMapper(ModelMapper modelMapper) {
        super(modelMapper, RestaurantTable.class);
    }

    public RestaurantTableDto.Response toResponse(RestaurantTable entity) {
        return super.toDto(entity, RestaurantTableDto.Response.class);
    }
}
