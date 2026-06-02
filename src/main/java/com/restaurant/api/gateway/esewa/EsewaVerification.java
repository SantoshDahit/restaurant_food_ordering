package com.restaurant.api.gateway.esewa;

import java.math.BigDecimal;

/**
 * Outcome of verifying an eSewa redirect-back payload.
 *
 * @param success         true only when signature + status check both pass
 * @param transactionUuid the merchant transaction id we sent (our payment code)
 * @param transactionCode eSewa's own reference for the settled transaction
 * @param totalAmount     the amount eSewa confirms was paid
 */
public record EsewaVerification(
        boolean success,
        String transactionUuid,
        String transactionCode,
        BigDecimal totalAmount
) {
}
