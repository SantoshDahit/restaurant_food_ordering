package com.restaurant.api.gateway.esewa;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * HMAC-SHA256 signing for eSewa ePay v2, base64-encoded. Pure and stateless —
 * no Spring, no domain knowledge — so it is trivially unit-testable.
 */
public final class EsewaSignatureUtil {

    private static final String ALGORITHM = "HmacSHA256";

    private EsewaSignatureUtil() {
    }

    /** Compute the base64 HMAC-SHA256 signature of {@code message} under {@code secretKey}. */
    public static String sign(String secretKey, String message) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to compute eSewa signature", e);
        }
    }

    /** True when {@code message} signs to exactly {@code expectedSignature}. */
    public static boolean matches(String secretKey, String message, String expectedSignature) {
        return sign(secretKey, message).equals(expectedSignature);
    }
}
