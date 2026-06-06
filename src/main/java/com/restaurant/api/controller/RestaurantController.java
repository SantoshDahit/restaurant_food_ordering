package com.restaurant.api.controller;

import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.service.facade.RestaurantFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantFacade restaurantFacade;

    @PostMapping
    public RestaurantDto.Response create(@Valid @RequestBody RestaurantDto.CreateRequest request) {
        return restaurantFacade.create(request);
    }

    @GetMapping("/{code}")
    public RestaurantDto.Response getByCode(@PathVariable String code) {
        return restaurantFacade.getByCode(code);
    }

    @GetMapping("/by-owner/{userCode}")
    public RestaurantDto.Response getByOwner(@PathVariable String userCode) {
        return restaurantFacade.getByUserCode(userCode);
    }

    @GetMapping("/by-kiosk-code/{kioskCode}")
    public RestaurantDto.Response getByKioskCode(@PathVariable String kioskCode) {
        return restaurantFacade.getByKioskCode(kioskCode);
    }

    @GetMapping("/search")
    public Page<RestaurantDto.Response> search(@ModelAttribute RestaurantDto.SearchRequest request,
                                               Pageable pageable) {
        return restaurantFacade.search(request, pageable);
    }

    @PatchMapping("/{code}")
    public RestaurantDto.Response update(@PathVariable String code,
                                         @RequestBody RestaurantDto.PatchRequest request) {
        return restaurantFacade.update(code, request);
    }

    /** Save this restaurant's Fonepay merchant credentials (encrypted at rest). Authenticated only. */
    @PatchMapping("/{code}/fonepay-credentials")
    public RestaurantDto.Response updateFonepayCredentials(
            @PathVariable String code,
            @Valid @RequestBody RestaurantDto.FonepayCredentialsRequest request) {
        return restaurantFacade.updateFonepayCredentials(code, request);
    }

    /** Save this restaurant's eSewa merchant credentials (encrypted at rest). Authenticated only. */
    @PatchMapping("/{code}/esewa-credentials")
    public RestaurantDto.Response updateEsewaCredentials(
            @PathVariable String code,
            @Valid @RequestBody RestaurantDto.EsewaCredentialsRequest request) {
        return restaurantFacade.updateEsewaCredentials(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        restaurantFacade.delete(code);
    }
}
