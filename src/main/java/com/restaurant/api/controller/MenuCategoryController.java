package com.restaurant.api.controller;

import com.restaurant.api.dto.MenuCategoryDto;
import com.restaurant.api.service.facade.MenuCategoryFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {
    private final MenuCategoryFacade menuCategoryFacade;

    @PostMapping
    public MenuCategoryDto.Response create(@Valid @RequestBody MenuCategoryDto.CreateRequest request) {
        return menuCategoryFacade.create(request);
    }

    @GetMapping("/{code}")
    public MenuCategoryDto.Response getByCode(@PathVariable String code) {
        return menuCategoryFacade.getByCode(code);
    }

    @GetMapping("/search")
    public Page<MenuCategoryDto.Response> search(@Valid @ModelAttribute MenuCategoryDto.SearchRequest request,
                                                 Pageable pageable) {
        return menuCategoryFacade.search(request, pageable);
    }

    @PatchMapping("/{code}")
    public MenuCategoryDto.Response update(@PathVariable String code,
                                           @RequestBody MenuCategoryDto.PatchRequest request) {
        return menuCategoryFacade.update(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        menuCategoryFacade.delete(code);
    }
}
