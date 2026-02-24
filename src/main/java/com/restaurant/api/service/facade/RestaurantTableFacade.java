package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.RestaurantTableDto;
import com.restaurant.api.entity.RestaurantTable;
import com.restaurant.api.mapper.RestaurantTableMapper;
import com.restaurant.api.service.RestaurantTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class RestaurantTableFacade {
    private final RestaurantTableService restaurantTableService;
    private final RestaurantTableMapper restaurantTableMapper;

    @Transactional
    public RestaurantTableDto.Response create(RestaurantTableDto.CreateRequest request) {
        RestaurantTable table = restaurantTableService.create(request);
        return restaurantTableMapper.toResponse(table);
    }

    @Transactional(readOnly = true)
    public RestaurantTableDto.Response getByCode(String code) {
        RestaurantTable table = restaurantTableService.getByCode(code);
        return restaurantTableMapper.toResponse(table);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantTableDto.Response> search(RestaurantTableDto.SearchRequest request, Pageable pageable) {
        return restaurantTableService.search(request, pageable)
                .map(restaurantTableMapper::toResponse);
    }

    @Transactional
    public RestaurantTableDto.Response update(String code, RestaurantTableDto.PatchRequest request) {
        RestaurantTable table = restaurantTableService.update(code, request);
        return restaurantTableMapper.toResponse(table);
    }

    @Transactional
    public void delete(String code) {
        restaurantTableService.delete(code);
    }

    @Transactional
    public RestaurantTableDto.Response generateQrCode(String code) {
        RestaurantTable table = restaurantTableService.generateQrToken(code);
        return restaurantTableMapper.toResponse(table);
    }

    @Transactional(readOnly = true)
    public RestaurantTableDto.Response getByQrToken(String token) {
        RestaurantTable table = restaurantTableService.getByQrToken(token);
        return restaurantTableMapper.toResponse(table);
    }
}
