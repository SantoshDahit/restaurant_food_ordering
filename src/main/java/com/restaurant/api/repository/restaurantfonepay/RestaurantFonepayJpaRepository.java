package com.restaurant.api.repository.restaurantfonepay;

import com.restaurant.api.entity.RestaurantFonepay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantFonepayJpaRepository extends JpaRepository<RestaurantFonepay, String> {
    Optional<RestaurantFonepay> findByRestaurantCode(String restaurantCode);
}
