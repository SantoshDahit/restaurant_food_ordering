package com.restaurant.api.repository.restaurantesewa;

import com.restaurant.api.entity.RestaurantEsewa;

import java.util.Optional;

public interface RestaurantEsewaRepository {
    Optional<RestaurantEsewa> findByRestaurantCode(String restaurantCode);
    RestaurantEsewa save(RestaurantEsewa entity);
}
