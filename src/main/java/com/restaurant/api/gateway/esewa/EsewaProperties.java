package com.restaurant.api.gateway.esewa;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * eSewa ePay v2 gateway configuration.
 *
 * <p>One switch — {@code payment.esewa.mode} — flips between the {@code test}
 * (UAT sandbox) and {@code production} credential blocks. The fixed eSewa URLs
 * are baked in per block; only the live merchant secrets come from env vars.
 * The rest of the gateway just calls {@link #getProductCode()} etc. and is
 * unaware of which mode is active.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.esewa")
public class EsewaProperties {

    public enum Mode {TEST, PRODUCTION}

    /** Which credential block is live. Defaults to the safe sandbox. */
    private Mode mode = Mode.TEST;

    private Credentials test = new Credentials();
    private Credentials production = new Credentials();

    @Getter
    @Setter
    public static class Credentials {
        /** Merchant product/service code. */
        private String productCode;
        /** HMAC-SHA256 signing secret. */
        private String secretKey;
        /** Hosted payment form URL the customer is redirected to. */
        private String formUrl;
        /** Transaction status-check API base URL (authoritative verification). */
        private String statusUrl;
    }

    private Credentials active() {
        return mode == Mode.PRODUCTION ? production : test;
    }

    public String getProductCode() {
        return active().getProductCode();
    }

    public String getSecretKey() {
        return active().getSecretKey();
    }

    public String getFormUrl() {
        return active().getFormUrl();
    }

    public String getStatusUrl() {
        return active().getStatusUrl();
    }

    /**
     * Fail fast at startup if the selected mode is missing the environment URLs.
     * Merchant secrets (product-code, secret-key) are NOT validated here — they
     * are now per-restaurant (see RestaurantEsewa), so only the global form/status
     * URLs must be present.
     */
    @PostConstruct
    void validate() {
        Credentials c = active();
        if (isBlank(c.getFormUrl()) || isBlank(c.getStatusUrl())) {
            throw new IllegalStateException(
                    "eSewa " + mode + " configuration is incomplete — "
                            + "form-url and status-url are required.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
