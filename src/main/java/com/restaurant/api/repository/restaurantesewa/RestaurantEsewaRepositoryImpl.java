package com.restaurant.api.repository.restaurantesewa;

import com.restaurant.api.entity.RestaurantEsewa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantEsewaRepositoryImpl implements RestaurantEsewaRepository {
    private final RestaurantEsewaJpaRepository jpaRepository;

    @Override
    public Optional<RestaurantEsewa> findByRestaurantCode(String restaurantCode) {
        return jpaRepository.findByRestaurantCode(restaurantCode);
    }

    @Override
    public RestaurantEsewa save(RestaurantEsewa entity) {
        return jpaRepository.save(entity);
    }
}
