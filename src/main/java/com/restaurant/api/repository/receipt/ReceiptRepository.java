package com.restaurant.api.repository.receipt;

import com.restaurant.api.entity.Receipt;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReceiptRepository {
    Optional<Receipt> findByCode(String code);
    Optional<Receipt> findByOrderCode(String orderCode);
    List<Receipt> findAllByOrderCodes(Collection<String> orderCodes);
    Optional<Integer> findMaxReceiptNumber(String restaurantCode, LocalDate businessDate);
    /** True if this daily receipt number is already taken (matches the uk_receipt_daily unique key). */
    boolean existsDaily(String restaurantCode, LocalDate businessDate, int receiptNumber);
    Receipt save(Receipt receipt);
}
