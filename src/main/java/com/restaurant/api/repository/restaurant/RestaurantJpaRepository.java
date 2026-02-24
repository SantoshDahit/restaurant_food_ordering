package com.restaurant.api.repository.restaurant;

import com.restaurant.api.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantJpaRepository extends JpaRepository<Restaurant, String> {
    Optional<Restaurant> findByCodeAndDeleteAtIsNull(String code);
    Optional<Restaurant> findByBusinessNumberAndDeleteAtIsNull(String businessNumber);
    boolean existsByBusinessNumberAndDeleteAtIsNull(String businessNumber);
    Optional<Restaurant> findByUserCodeAndDeleteAtIsNull(String userCode);
}
