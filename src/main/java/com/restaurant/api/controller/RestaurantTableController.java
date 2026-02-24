package com.restaurant.api.controller;

import com.restaurant.api.dto.RestaurantTableDto;
import com.restaurant.api.service.facade.RestaurantTableFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tables")
@RequiredArgsConstructor
public class RestaurantTableController {
    private final RestaurantTableFacade restaurantTableFacade;

    @PostMapping
    public RestaurantTableDto.Response create(@Valid @RequestBody RestaurantTableDto.CreateRequest request) {
        return restaurantTableFacade.create(request);
    }

    @GetMapping("/{code}")
    public RestaurantTableDto.Response getByCode(@PathVariable String code) {
        return restaurantTableFacade.getByCode(code);
    }

    @GetMapping("/search")
    public Page<RestaurantTableDto.Response> search(@ModelAttribute RestaurantTableDto.SearchRequest request,
                                                    Pageable pageable) {
        return restaurantTableFacade.search(request, pageable);
    }

    @PatchMapping("/{code}")
    public RestaurantTableDto.Response update(@PathVariable String code,
                                              @RequestBody RestaurantTableDto.PatchRequest request) {
        return restaurantTableFacade.update(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        restaurantTableFacade.delete(code);
    }

    @PostMapping("/{code}/qr")
    public RestaurantTableDto.Response generateQrCode(@PathVariable String code) {
        return restaurantTableFacade.generateQrCode(code);
    }

    @GetMapping("/by-token/{token}")
    public RestaurantTableDto.Response getByQrToken(@PathVariable String token) {
        return restaurantTableFacade.getByQrToken(token);
    }
}
