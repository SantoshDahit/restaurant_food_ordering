package com.restaurant.api.repository.menucategory;

import com.restaurant.api.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuCategoryJpaRepository extends JpaRepository<MenuCategory, String> {
    Optional<MenuCategory> findByCodeAndDeleteAtIsNull(String code);
    boolean existsByRestaurantCodeAndNameAndDeleteAtIsNull(String restaurantCode, String name);
}
