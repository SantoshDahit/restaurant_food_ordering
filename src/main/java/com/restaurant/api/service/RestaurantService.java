package com.restaurant.api.service;

import com.restaurant.api.common.RestaurantCodeGenerator;
import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.entity.User;
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
    private final UserService userService;

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
    public Restaurant create(RestaurantDto.CreateRequest request) {

        User user = userService.getByCode(request.userCode());

        Restaurant restaurant = new Restaurant(
                user,
                request.name(),
                request.address(),
                request.businessNumber(),
                request.phone(),
                request.email(),
                request.currency(),
                generateUniqueKioskCode()
        );
        return restaurantRepository.save(restaurant);
    }

    private String generateUniqueKioskCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = RestaurantCodeGenerator.generate();
            if (restaurantRepository.findByKioskCode(code).isEmpty()) {
                return code;
            }
        }
        throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Transactional(readOnly = true)
    public Restaurant getByKioskCode(String kioskCode) {
        return restaurantRepository.findByKioskCode(kioskCode)
                .orElseThrow(() -> new ApiException(ErrorCode.RESTAURANT_NOT_FOUND));
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
