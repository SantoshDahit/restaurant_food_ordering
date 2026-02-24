package com.restaurant.api.repository.table;

import com.restaurant.api.dto.RestaurantTableDto;
import com.restaurant.api.entity.RestaurantTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RestaurantTableRepository {
    Optional<RestaurantTable> findByCode(String code);
    Optional<RestaurantTable> findByRestaurantCodeAndTableNumber(String restaurantCode, String tableNumber);
    Optional<RestaurantTable> findByQrToken(String token);
    RestaurantTable save(RestaurantTable restaurantTable);
    Page<RestaurantTable> search(RestaurantTableDto.SearchRequest searchRequest, Pageable pageable);
}
