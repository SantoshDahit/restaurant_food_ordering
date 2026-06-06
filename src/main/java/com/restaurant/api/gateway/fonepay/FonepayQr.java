package com.restaurant.api.gateway.fonepay;

/**
 * Result of a dynamic-QR download. {@code qrMessage} is the raw string the
 * frontend renders into a scannable QR; {@code websocketUrl} streams live
 * payment status; {@code prn} is the merchant transaction reference we sent.
 */
public record FonepayQr(
        String qrMessage,
        String websocketUrl,
        String prn
) {
}
