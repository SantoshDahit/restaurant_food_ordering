package com.restaurant.api.repository.menuitem;

import com.restaurant.api.dto.MenuItemDto;
import com.restaurant.api.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MenuItemRepository {
    Optional<MenuItem> findByCode(String code);
    MenuItem save(MenuItem menuItem);
    Page<MenuItem> search(MenuItemDto.SearchRequest searchRequest, Pageable pageable);
}
