package com.restaurant.api.repository.receipt;

import com.restaurant.api.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReceiptJpaRepository extends JpaRepository<Receipt, String> {
    Optional<Receipt> findByCodeAndDeletedAtIsNull(String code);

    Optional<Receipt> findByOrderCodeAndDeletedAtIsNull(String orderCode);

    List<Receipt> findAllByOrderCodeInAndDeletedAtIsNull(Collection<String> orderCodes);

    /** Highest-numbered receipt for the given restaurant on the given business day. */
    Optional<Receipt> findTopByRestaurantCodeAndBusinessDateOrderByReceiptNumberDesc(
            String restaurantCode, LocalDate businessDate);
}
