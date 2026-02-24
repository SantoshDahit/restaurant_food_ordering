package com.restaurant.api.repository.restaurant;

import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantQueryRepository restaurantQueryRepository;

    @Override
    public Optional<Restaurant> findByCode(String code) {
        return restaurantJpaRepository.findByCodeAndDeleteAtIsNull(code);
    }

    @Override
    public Optional<Restaurant> findByBusinessNumber(String businessNumber) {
        return restaurantJpaRepository.findByBusinessNumberAndDeleteAtIsNull(businessNumber);
    }

    @Override
    public Optional<Restaurant> findByUserCode(String userCode) {
        return restaurantJpaRepository.findByUserCodeAndDeleteAtIsNull(userCode);
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        return restaurantJpaRepository.save(restaurant);
    }

    @Override
    public Page<Restaurant> search(RestaurantDto.SearchRequest searchRequest, Pageable pageable) {
        return restaurantQueryRepository.search(searchRequest, pageable);
    }
}
