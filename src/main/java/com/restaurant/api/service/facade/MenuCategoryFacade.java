package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.MenuCategoryDto;
import com.restaurant.api.entity.MenuCategory;
import com.restaurant.api.mapper.MenuCategoryMapper;
import com.restaurant.api.service.MenuCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class MenuCategoryFacade {
    private final MenuCategoryService menuCategoryService;
    private final MenuCategoryMapper menuCategoryMapper;

    @Transactional
    public MenuCategoryDto.Response create(MenuCategoryDto.CreateRequest request) {
        MenuCategory menuCategory = menuCategoryService.create(request);
        return menuCategoryMapper.toResponse(menuCategory);
    }

    @Transactional(readOnly = true)
    public MenuCategoryDto.Response getByCode(String code) {
        MenuCategory menuCategory = menuCategoryService.getByCode(code);
        return menuCategoryMapper.toResponse(menuCategory);
    }

    @Transactional(readOnly = true)
    public Page<MenuCategoryDto.Response> search(MenuCategoryDto.SearchRequest request, Pageable pageable) {
        return menuCategoryService.search(request, pageable)
                .map(menuCategoryMapper::toResponse);
    }

    @Transactional
    public MenuCategoryDto.Response update(String code, MenuCategoryDto.PatchRequest request) {
        MenuCategory menuCategory = menuCategoryService.update(code, request);
        return menuCategoryMapper.toResponse(menuCategory);
    }

    @Transactional
    public void delete(String code) {
        menuCategoryService.delete(code);
    }
}
