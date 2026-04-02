package com.restaurant.api.repository.menuitem;

import com.restaurant.api.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MenuItemJpaRepository extends JpaRepository<MenuItem, String> {
    Optional<MenuItem> findByCodeAndDeletedAtIsNull(String code);
    boolean existsByRestaurantCodeAndNameAndDeletedAtIsNull(String restaurantCode, String name);
    boolean existsByCategoryCodeAndDeletedAtIsNull(String categoryCode);
}
