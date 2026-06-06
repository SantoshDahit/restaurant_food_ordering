package com.restaurant.api.repository.restaurantfonepay;

import com.restaurant.api.entity.RestaurantFonepay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantFonepayRepositoryImpl implements RestaurantFonepayRepository {
    private final RestaurantFonepayJpaRepository jpaRepository;

    @Override
    public Optional<RestaurantFonepay> findByRestaurantCode(String restaurantCode) {
        return jpaRepository.findByRestaurantCode(restaurantCode);
    }

    @Override
    public RestaurantFonepay save(RestaurantFonepay entity) {
        return jpaRepository.save(entity);
    }
}
