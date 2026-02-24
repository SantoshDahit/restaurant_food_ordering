package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public Restaurant getByCode(String code) {
        return restaurantRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Restaurant getByUserCode(String userCode) {
        return restaurantRepository.findByUserCode(userCode)
                .orElseThrow(() -> new ApiException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Restaurant> search(RestaurantDto.SearchRequest searchRequest, Pageable pageable) {
        return restaurantRepository.search(searchRequest, pageable);
    }

    @Transactional
    public Restaurant create(RestaurantDto.CreateRequest request, String userCode) {
        String code = (request.code() != null && !request.code().isBlank())
                ? request.code()
                : UuidUtil.generate();
        Restaurant restaurant = new Restaurant(
                code,
                userCode,
                request.name(),
                request.address(),
                request.businessNumber(),
                request.phone(),
                request.email(),
                request.currency()
        );
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant update(String code, RestaurantDto.PatchRequest request) {
        Restaurant restaurant = getByCode(code);
        restaurant.update(request.name(), request.address(), request.phone(),
                request.email(), request.currency(), request.fileCode());
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public void delete(String code) {
        Restaurant restaurant = getByCode(code);
        restaurant.deactivate();
        restaurantRepository.save(restaurant);
    }
}
