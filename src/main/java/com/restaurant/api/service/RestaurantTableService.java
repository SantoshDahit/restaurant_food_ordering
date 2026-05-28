package com.restaurant.api.service;

import com.restaurant.api.common.TableCodeGenerator;
import com.restaurant.api.common.UuidUtil;
import com.restaurant.api.dto.RestaurantTableDto;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.entity.RestaurantTable;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.repository.restaurant.RestaurantRepository;
import com.restaurant.api.repository.table.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {
    private final RestaurantTableRepository restaurantTableRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public RestaurantTable getByCode(String code) {
        return restaurantTableRepository.findByCode(code)
                .orElseThrow(() -> new ApiException(ErrorCode.TABLE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public RestaurantTable getByTableCode(String tableCode) {
        return restaurantTableRepository.findByTableCode(tableCode)
                .orElseThrow(() -> new ApiException(ErrorCode.TABLE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public RestaurantTable getByQrToken(String token) {
        return restaurantTableRepository.findByQrToken(token)
                .orElseThrow(() -> new ApiException(ErrorCode.TABLE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<RestaurantTable> search(RestaurantTableDto.SearchRequest searchRequest, Pageable pageable) {
        return restaurantTableRepository.search(searchRequest, pageable);
    }

    @Transactional
    public RestaurantTable create(RestaurantTableDto.CreateRequest request) {
        restaurantTableRepository.findByRestaurantCodeAndTableNumber(
                request.restaurantCode(), request.tableNumber()
        ).ifPresent(existing -> {
            throw new ApiException(ErrorCode.TABLE_NUMBER_ALREADY_EXISTS);
        });
        Restaurant restaurant = restaurantRepository.findByCode(request.restaurantCode())
                .orElseThrow(() -> new ApiException(ErrorCode.RESTAURANT_NOT_FOUND));
        RestaurantTable table = new RestaurantTable(
                request.restaurantCode(),
                request.tableNumber(),
                request.capacity(),
                generateUniqueTableCode(restaurant.getName())
        );
        return restaurantTableRepository.save(table);
    }

    private String generateUniqueTableCode(String restaurantName) {
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = TableCodeGenerator.generate(restaurantName);
            if (!restaurantTableRepository.existsByTableCode(candidate)) {
                return candidate;
            }
        }
        throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Transactional
    public RestaurantTable update(String code, RestaurantTableDto.PatchRequest request) {
        RestaurantTable table = getByCode(code);
        table.update(request.tableNumber(), request.capacity(), request.status());
        return restaurantTableRepository.save(table);
    }

    @Transactional
    public void delete(String code) {
        RestaurantTable table = getByCode(code);
        table.deactivate();
        restaurantTableRepository.save(table);
    }

    @Transactional
    public RestaurantTable generateQrToken(String code) {
        RestaurantTable table = getByCode(code);
        String token = UuidUtil.generate();
        String qrCodeUrl = "/qr/" + token;
        table.updateQrCode(token, qrCodeUrl);
        return restaurantTableRepository.save(table);
    }
}
