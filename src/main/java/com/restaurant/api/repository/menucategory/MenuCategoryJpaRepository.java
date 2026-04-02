package com.restaurant.api.repository.menucategory;

import com.restaurant.api.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuCategoryJpaRepository extends JpaRepository<MenuCategory, String> {
    Optional<MenuCategory> findByCodeAndDeletedAtIsNull(String code);
    boolean existsByRestaurantCodeAndNameAndDeletedAtIsNull(String restaurantCode, String name);
}
