package com.restaurant.api.repository.menucategory;

import com.restaurant.api.dto.MenuCategoryDto;
import com.restaurant.api.entity.MenuCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuCategoryRepositoryImpl implements MenuCategoryRepository {
    private final MenuCategoryJpaRepository menuCategoryJpaRepository;
    private final MenuCategoryQueryRepository menuCategoryQueryRepository;

    @Override
    public Optional<MenuCategory> findByCode(String code) {
        return menuCategoryJpaRepository.findByCodeAndDeleteAtIsNull(code);
    }

    @Override
    public MenuCategory save(MenuCategory menuCategory) {
        return menuCategoryJpaRepository.save(menuCategory);
    }

    @Override
    public Page<MenuCategory> search(MenuCategoryDto.SearchRequest searchRequest, Pageable pageable) {
        return menuCategoryQueryRepository.search(searchRequest, pageable);
    }
}
