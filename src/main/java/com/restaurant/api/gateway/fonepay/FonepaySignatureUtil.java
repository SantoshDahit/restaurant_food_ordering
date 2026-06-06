package com.restaurant.api.gateway.fonepay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * HMAC-SHA512 signing for Fonepay's {@code dataValidation} field, lowercase-hex
 * encoded. Pure and stateless — no Spring, no domain knowledge.
 *
 * <p>The signed message is a comma-joined field set, exact order mandated by
 * Fonepay (QR: {@code amount,prn,merchantCode,remarks1,remarks2}; status:
 * {@code prn,merchantCode}). The key is the merchant's secret key.
 */
public final class FonepaySignatureUtil {

    private static final String ALGORITHM = "HmacSHA512";

    private FonepaySignatureUtil() {
    }

    /** Lowercase-hex HMAC-SHA512 of {@code message} under {@code secretKey}. */
    public static String sign(String secretKey, String message) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to compute Fonepay signature", e);
        }
    }

    /** True when {@code message} signs to exactly {@code expectedSignature} (case-insensitive hex). */
    public static boolean matches(String secretKey, String message, String expectedSignature) {
        return expectedSignature != null && sign(secretKey, message).equalsIgnoreCase(expectedSignature);
    }
}
