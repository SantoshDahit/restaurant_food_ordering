package com.restaurant.api.repository.menucategory;

import com.restaurant.api.dto.MenuCategoryDto;
import com.restaurant.api.entity.MenuCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MenuCategoryRepository {
    Optional<MenuCategory> findByCode(String code);
    MenuCategory save(MenuCategory menuCategory);
    Page<MenuCategory> search(MenuCategoryDto.SearchRequest searchRequest, Pageable pageable);
}
