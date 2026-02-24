package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.MenuCategoryDto;
import com.restaurant.api.entity.MenuCategory;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.menucategory.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {
    private final MenuCategoryRepository menuCategoryRepository;

    @Transactional(readOnly = true)
    public MenuCategory getByCode(String code) {
        return menuCategoryRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<MenuCategory> search(MenuCategoryDto.SearchRequest searchRequest, Pageable pageable) {
        return menuCategoryRepository.search(searchRequest, pageable);
    }

    @Transactional
    public MenuCategory create(MenuCategoryDto.CreateRequest request) {
        MenuCategory menuCategory = new MenuCategory(
                UuidUtil.generate(),
                request.restaurantCode(),
                request.name(),
                request.categoryType(),
                request.sortOrder()
        );
        return menuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public MenuCategory update(String code, MenuCategoryDto.PatchRequest request) {
        MenuCategory menuCategory = getByCode(code);
        menuCategory.update(request.name(), request.categoryType(), request.fileCode(), request.sortOrder());
        return menuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public void delete(String code) {
        MenuCategory menuCategory = getByCode(code);
        menuCategory.deactivate();
        menuCategoryRepository.save(menuCategory);
    }
}
