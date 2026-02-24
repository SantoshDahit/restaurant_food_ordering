package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.MenuItemDto;
import com.restaurant.api.entity.MenuItem;
import com.restaurant.api.mapper.MenuItemMapper;
import com.restaurant.api.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class MenuItemFacade {
    private final MenuItemService menuItemService;
    private final MenuItemMapper menuItemMapper;

    @Transactional
    public MenuItemDto.Response create(MenuItemDto.CreateRequest request) {
        MenuItem menuItem = menuItemService.create(request);
        return menuItemMapper.toResponse(menuItem);
    }

    @Transactional(readOnly = true)
    public MenuItemDto.Response getByCode(String code) {
        MenuItem menuItem = menuItemService.getByCode(code);
        return menuItemMapper.toResponse(menuItem);
    }

    @Transactional(readOnly = true)
    public Page<MenuItemDto.Response> search(MenuItemDto.SearchRequest request, Pageable pageable) {
        return menuItemService.search(request, pageable)
                .map(menuItemMapper::toResponse);
    }

    @Transactional
    public MenuItemDto.Response update(String code, MenuItemDto.PatchRequest request) {
        MenuItem menuItem = menuItemService.update(code, request);
        return menuItemMapper.toResponse(menuItem);
    }

    @Transactional
    public void delete(String code) {
        menuItemService.delete(code);
    }
}
