package com.restaurant.api.repository.table;

import com.restaurant.api.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantTableJpaRepository extends JpaRepository<RestaurantTable, String> {
    Optional<RestaurantTable> findByCodeAndDeletedAtIsNull(String code);
    Optional<RestaurantTable> findByRestaurantCodeAndTableNumberAndDeletedAtIsNull(String restaurantCode, String tableNumber);
    boolean existsByRestaurantCodeAndTableNumberAndDeletedAtIsNull(String restaurantCode, String tableNumber);
    Optional<RestaurantTable> findByQrCodeTokenAndDeletedAtIsNull(String qrCodeToken);
}
