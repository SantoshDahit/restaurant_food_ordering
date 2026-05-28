package com.restaurant.api.service;

import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.MenuItemDto;
import com.restaurant.api.entity.MenuItem;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.menuitem.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public MenuItem getByCode(String code) {
        return menuItemRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.MENU_ITEM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<MenuItem> search(MenuItemDto.SearchRequest searchRequest, Pageable pageable) {
        return menuItemRepository.search(searchRequest, pageable);
    }

    @Transactional
    public MenuItem create(MenuItemDto.CreateRequest request) {
        MenuItem menuItem = new MenuItem(
                UuidUtil.generate(),
                request.restaurantCode(),
                request.categoryCode(),
                request.name(),
                request.description(),
                request.price(),
                request.discountPercent(),
                request.fileCode(),
                request.isVeg(),
                request.prepTimeMinutes(),
                request.sortOrder(),
                request.availability()
        );
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public MenuItem update(String code, MenuItemDto.PatchRequest request) {
        MenuItem menuItem = getByCode(code);
        menuItem.update(request.name(), request.description(), request.price(),
                request.discountPercent(), request.fileCode(), request.availability(),
                request.isFeatured(), request.isVeg(), request.prepTimeMinutes(),
                request.sortOrder(), request.categoryCode());
        return menuItemRepository.save(menuItem);
    }

    @Transactional
    public void delete(String code) {
        MenuItem menuItem = getByCode(code);
        menuItem.delete();
        menuItemRepository.save(menuItem);
    }
}
