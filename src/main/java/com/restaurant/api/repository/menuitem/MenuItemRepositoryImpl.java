package com.restaurant.api.repository.menuitem;

import com.restaurant.api.dto.MenuItemDto;
import com.restaurant.api.entity.MenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MenuItemRepositoryImpl implements MenuItemRepository {
    private final MenuItemJpaRepository menuItemJpaRepository;
    private final MenuItemQueryRepository menuItemQueryRepository;

    @Override
    public Optional<MenuItem> findByCode(String code) {
        return menuItemJpaRepository.findByCodeAndDeletedAtIsNull(code);
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        return menuItemJpaRepository.save(menuItem);
    }

    @Override
    public Page<MenuItem> search(MenuItemDto.SearchRequest searchRequest, Pageable pageable) {
        return menuItemQueryRepository.search(searchRequest, pageable);
    }
}
