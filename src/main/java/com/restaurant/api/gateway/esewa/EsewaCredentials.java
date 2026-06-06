package com.restaurant.api.gateway.esewa;

/**
 * Decrypted eSewa merchant credentials for a single restaurant, passed into the
 * gateway. The environment URLs (form/status) are NOT here — they stay global
 * and mode-switched in {@link EsewaProperties}; only the per-merchant secrets
 * vary by restaurant. Plain value object — never serialized to clients or logged.
 */
public record EsewaCredentials(
        String productCode,
        String secretKey
) {
}
