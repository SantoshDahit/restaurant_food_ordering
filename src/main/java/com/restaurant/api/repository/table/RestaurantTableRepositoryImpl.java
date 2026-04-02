package com.restaurant.api.repository.table;

import com.restaurant.api.dto.RestaurantTableDto;
import com.restaurant.api.entity.RestaurantTable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RestaurantTableRepositoryImpl implements RestaurantTableRepository {
    private final RestaurantTableJpaRepository restaurantTableJpaRepository;
    private final RestaurantTableQueryRepository restaurantTableQueryRepository;

    @Override
    public Optional<RestaurantTable> findByCode(String code) {
        return restaurantTableJpaRepository.findByCodeAndDeletedAtIsNull(code);
    }

    @Override
    public Optional<RestaurantTable> findByRestaurantCodeAndTableNumber(String restaurantCode, String tableNumber) {
        return restaurantTableJpaRepository.findByRestaurantCodeAndTableNumberAndDeletedAtIsNull(restaurantCode, tableNumber);
    }

    @Override
    public Optional<RestaurantTable> findByQrToken(String token) {
        return restaurantTableJpaRepository.findByQrCodeTokenAndDeletedAtIsNull(token);
    }

    @Override
    public RestaurantTable save(RestaurantTable restaurantTable) {
        return restaurantTableJpaRepository.save(restaurantTable);
    }

    @Override
    public Page<RestaurantTable> search(RestaurantTableDto.SearchRequest searchRequest, Pageable pageable) {
        return restaurantTableQueryRepository.search(searchRequest, pageable);
    }
}
