package com.restaurant.api.service.facade;

import com.restaurant.api.annotation.Facade;
import com.restaurant.api.dto.ReceiptDto;
import com.restaurant.api.entity.Receipt;
import com.restaurant.api.entity.Restaurant;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.mapper.ReceiptMapper;
import com.restaurant.api.repository.restaurant.RestaurantRepository;
import com.restaurant.api.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Facade
@RequiredArgsConstructor
public class ReceiptFacade {
    private final ReceiptService receiptService;
    private final ReceiptMapper receiptMapper;
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public ReceiptDto.Response getByCode(String code) {
        return toResponseWithRestaurantInfo(receiptService.getByCode(code));
    }

    @Transactional(readOnly = true)
    public ReceiptDto.Response getByOrderCode(String orderCode) {
        Receipt receipt = receiptService.findByOrderCode(orderCode)
                .orElseThrow(() -> new ApiException(ErrorCode.RECEIPT_NOT_FOUND));
        return toResponseWithRestaurantInfo(receipt);
    }

    private ReceiptDto.Response toResponseWithRestaurantInfo(Receipt receipt) {
        ReceiptDto.Response r = receiptMapper.toResponse(receipt);
        restaurantRepository.findByCode(receipt.getRestaurantCode()).ifPresent((Restaurant rest) -> {
            r.setRestaurantAddress(rest.getAddress());
            r.setRestaurantPhone(rest.getPhone());
            r.setRestaurantBusinessNumber(rest.getBusinessNumber());
        });
        return r;
    }
}
