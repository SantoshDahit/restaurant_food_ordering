package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.mapper.RestaurantMapper;
import com.restaurant.api.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class RestaurantFacade {
    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;

    @Transactional
    public RestaurantDto.Response create(RestaurantDto.CreateRequest request) {
        Restaurant restaurant = restaurantService.create(request);
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantDto.Response getByCode(String code) {
        Restaurant restaurant = restaurantService.getByCode(code);
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantDto.Response getByUserCode(String userCode) {
        Restaurant restaurant = restaurantService.getByUserCode(userCode);
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantDto.Response getByKioskCode(String kioskCode) {
        Restaurant restaurant = restaurantService.getByKioskCode(kioskCode);
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantDto.Response> search(RestaurantDto.SearchRequest request, Pageable pageable) {
        return restaurantService.search(request, pageable)
                .map(restaurantMapper::toResponse);
    }

    @Transactional
    public RestaurantDto.Response update(String code, RestaurantDto.PatchRequest request) {
        Restaurant restaurant = restaurantService.update(code, request);
        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional
    public void delete(String code) {
        restaurantService.delete(code);
    }
}
