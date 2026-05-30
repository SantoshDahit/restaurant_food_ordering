package com.restaurant.api.controller;

import com.restaurant.api.dto.MenuItemDto;
import com.restaurant.api.service.facade.MenuItemFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/menu-items")
@RequiredArgsConstructor
public class MenuItemController {
    private final MenuItemFacade menuItemFacade;

    @PostMapping
    public MenuItemDto.Response create(@Valid @RequestBody MenuItemDto.CreateRequest request) {
        return menuItemFacade.create(request);
    }

    @GetMapping("/{code}")
    public MenuItemDto.Response getByCode(@PathVariable String code) {
        return menuItemFacade.getByCode(code);
    }

    @GetMapping("/search")
    public Page<MenuItemDto.Response> search(@Valid @ModelAttribute MenuItemDto.SearchRequest request,
                                             Pageable pageable) {
        return menuItemFacade.search(request, pageable);
    }

    @PatchMapping("/{code}")
    public MenuItemDto.Response update(@PathVariable String code,
                                       @RequestBody MenuItemDto.PatchRequest request) {
        return menuItemFacade.update(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        menuItemFacade.delete(code);
    }
}
