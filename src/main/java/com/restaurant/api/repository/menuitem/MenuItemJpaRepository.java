package com.restaurant.api.repository.menuitem;

import com.restaurant.api.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuItemJpaRepository extends JpaRepository<MenuItem, String> {
    Optional<MenuItem> findByCodeAndDeleteAtIsNull(String code);
    boolean existsByRestaurantCodeAndNameAndDeleteAtIsNull(String restaurantCode, String name);
    boolean existsByCategoryCodeAndDeleteAtIsNull(String categoryCode);
}
