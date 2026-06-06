package com.restaurant.api.gateway.fonepay;

/**
 * Outcome of checking a Fonepay transaction's settled status.
 *
 * @param success       true only when Fonepay reports the payment as settled
 * @param prn           the merchant transaction reference
 * @param traceId       Fonepay's own trace id for the settled transaction
 * @param paymentStatus the raw status string Fonepay returned
 */
public record FonepayVerification(
        boolean success,
        String prn,
        String traceId,
        String paymentStatus
) {
}
