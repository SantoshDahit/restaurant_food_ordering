package com.restaurant.api.repository.table;

import com.restaurant.api.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantTableJpaRepository extends JpaRepository<RestaurantTable, String> {
    Optional<RestaurantTable> findByCodeAndDeleteAtIsNull(String code);
    Optional<RestaurantTable> findByRestaurantCodeAndTableNumberAndDeleteAtIsNull(String restaurantCode, String tableNumber);
    boolean existsByRestaurantCodeAndTableNumberAndDeleteAtIsNull(String restaurantCode, String tableNumber);
    Optional<RestaurantTable> findByQrCodeTokenAndDeleteAtIsNull(String qrCodeToken);
}
