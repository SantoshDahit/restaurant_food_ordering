package com.restaurant.api.repository.restaurantesewa;

import com.restaurant.api.entity.RestaurantEsewa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantEsewaJpaRepository extends JpaRepository<RestaurantEsewa, String> {
    Optional<RestaurantEsewa> findByRestaurantCode(String restaurantCode);
}
