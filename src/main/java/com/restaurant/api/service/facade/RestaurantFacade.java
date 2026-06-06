package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.mapper.RestaurantMapper;
import com.restaurant.api.service.RestaurantEsewaService;
import com.restaurant.api.service.RestaurantFonepayService;
import com.restaurant.api.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class RestaurantFacade {
    private final RestaurantService restaurantService;
    private final RestaurantFonepayService restaurantFonepayService;
    private final RestaurantEsewaService restaurantEsewaService;
    private final RestaurantMapper restaurantMapper;

    @Transactional
    public RestaurantDto.Response create(RestaurantDto.CreateRequest request) {
        return toResponse(restaurantService.create(request));
    }

    @Transactional(readOnly = true)
    public RestaurantDto.Response getByCode(String code) {
        return toResponse(restaurantService.getByCode(code));
    }

    @Transactional(readOnly = true)
    public RestaurantDto.Response getByUserCode(String userCode) {
        return toResponse(restaurantService.getByUserCode(userCode));
    }

    @Transactional(readOnly = true)
    public RestaurantDto.Response getByKioskCode(String kioskCode) {
        return toResponse(restaurantService.getByKioskCode(kioskCode));
    }

    @Transactional(readOnly = true)
    public Page<RestaurantDto.Response> search(RestaurantDto.SearchRequest request, Pageable pageable) {
        // List view doesn't need the payment flags — skip the per-row lookup (avoids N+1).
        return restaurantService.search(request, pageable).map(restaurantMapper::toResponse);
    }

    @Transactional
    public RestaurantDto.Response update(String code, RestaurantDto.PatchRequest request) {
        return toResponse(restaurantService.update(code, request));
    }

    @Transactional
    public RestaurantDto.Response updateFonepayCredentials(String code, RestaurantDto.FonepayCredentialsRequest request) {
        Restaurant restaurant = restaurantService.getByCode(code); // 404 if the restaurant is bogus
        restaurantFonepayService.save(code, request);
        return toResponse(restaurant);
    }

    @Transactional
    public RestaurantDto.Response updateEsewaCredentials(String code, RestaurantDto.EsewaCredentialsRequest request) {
        Restaurant restaurant = restaurantService.getByCode(code); // 404 if the restaurant is bogus
        restaurantEsewaService.save(code, request);
        return toResponse(restaurant);
    }

    @Transactional
    public void delete(String code) {
        restaurantService.delete(code);
    }

    /** Map to the response and stamp the (secret-free) payment-config status flags. */
    private RestaurantDto.Response toResponse(Restaurant restaurant) {
        RestaurantDto.Response response = restaurantMapper.toResponse(restaurant);
        restaurantFonepayService.find(restaurant.getCode()).ifPresentOrElse(
                fp -> {
                    response.setFonepayConfigured(true);
                    response.setFonepayEnabled(fp.getEnabled());
                },
                () -> {
                    response.setFonepayConfigured(false);
                    response.setFonepayEnabled(false);
                });
        restaurantEsewaService.find(restaurant.getCode()).ifPresentOrElse(
                es -> {
                    response.setEsewaConfigured(true);
                    response.setEsewaEnabled(es.getEnabled());
                },
                () -> {
                    response.setEsewaConfigured(false);
                    response.setEsewaEnabled(false);
                });
        return response;
    }
}
