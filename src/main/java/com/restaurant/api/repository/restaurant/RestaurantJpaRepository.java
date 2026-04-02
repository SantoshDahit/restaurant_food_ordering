package com.restaurant.api.repository.restaurant;

import com.restaurant.api.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantJpaRepository extends JpaRepository<Restaurant, String> {
    Optional<Restaurant> findByCodeAndDeletedAtIsNull(String code);
    Optional<Restaurant> findByBusinessNumberAndDeletedAtIsNull(String businessNumber);
    boolean existsByBusinessNumberAndDeletedAtIsNull(String businessNumber);
    Optional<Restaurant> findByUserCodeAndDeletedAtIsNull(String userCode);
}
