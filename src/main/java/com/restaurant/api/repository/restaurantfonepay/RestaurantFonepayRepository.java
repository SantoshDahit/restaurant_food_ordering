package com.restaurant.api.repository.restaurantfonepay;

import com.restaurant.api.entity.RestaurantFonepay;

import java.util.Optional;

public interface RestaurantFonepayRepository {
    Optional<RestaurantFonepay> findByRestaurantCode(String restaurantCode);
    RestaurantFonepay save(RestaurantFonepay entity);
}
