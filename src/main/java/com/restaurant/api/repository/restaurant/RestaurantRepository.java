package com.restaurant.api.repository.restaurant;

import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findByCode(String code);
    Optional<Restaurant> findByBusinessNumber(String businessNumber);
    Optional<Restaurant> findByUserCode(String userCode);
    Restaurant save(Restaurant restaurant);
    Page<Restaurant> search(RestaurantDto.SearchRequest searchRequest, Pageable pageable);
}
