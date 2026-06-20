package com.restaurant.api.repository.receipt;

import com.restaurant.api.entity.Receipt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReceiptRepositoryImpl implements ReceiptRepository {
    private final ReceiptJpaRepository receiptJpaRepository;

    @Override
    public Optional<Receipt> findByCode(String code) {
        return receiptJpaRepository.findByCodeAndDeletedAtIsNull(code);
    }

    @Override
    public Optional<Receipt> findByOrderCode(String orderCode) {
        return receiptJpaRepository.findByOrderCodeAndDeletedAtIsNull(orderCode);
    }

    @Override
    public List<Receipt> findAllByOrderCodes(Collection<String> orderCodes) {
        if (orderCodes == null || orderCodes.isEmpty()) return Collections.emptyList();
        return receiptJpaRepository.findAllByOrderCodeInAndDeletedAtIsNull(orderCodes);
    }

    @Override
    public Optional<Integer> findMaxReceiptNumber(String restaurantCode, LocalDate businessDate) {
        return receiptJpaRepository
                .findTopByRestaurantCodeAndBusinessDateOrderByReceiptNumberDesc(restaurantCode, businessDate)
                .map(Receipt::getReceiptNumber);
    }

    @Override
    public boolean existsDaily(String restaurantCode, LocalDate businessDate, int receiptNumber) {
        return receiptJpaRepository
                .existsByRestaurantCodeAndBusinessDateAndReceiptNumber(restaurantCode, businessDate, receiptNumber);
    }

    @Override
    public Receipt save(Receipt receipt) {
        return receiptJpaRepository.save(receipt);
    }
}
