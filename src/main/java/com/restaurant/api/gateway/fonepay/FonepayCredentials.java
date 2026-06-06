package com.restaurant.api.gateway.fonepay;

/**
 * Decrypted Fonepay merchant credentials for a single restaurant, passed into
 * the gateway. Plain value object — never serialized to clients or logged.
 */
public record FonepayCredentials(
        String merchantCode,
        String username,
        String password,
        String secretKey
) {
}
