package com.restaurant.api.entity;

import com.restaurant.api.entity.base.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * A restaurant's eSewa ePay merchant credentials (one row per restaurant, keyed
 * by restaurant code). Mirrors {@link RestaurantFonepay}: payment-gateway secrets
 * live in their own domain/table so they don't bloat the core Restaurant entity,
 * and each tenant settles to its own eSewa merchant account.
 *
 * <p>{@code productCode} is a public-ish merchant identifier (stored plain);
 * {@code secretKey} is SENSITIVE and stored ENCRYPTED — the service encrypts it
 * via CredentialCipher before persisting. The environment URLs are NOT here —
 * they stay global (mode-switched) in EsewaProperties.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "restaurant_esewa")
@EntityListeners(AuditingEntityListener.class)
public class RestaurantEsewa extends BaseTimeEntity {

    @Id
    @Column(name = "restaurant_code")
    private String restaurantCode;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(name = "secret_key", nullable = false, length = 512)
    private String secretKey;    // encrypted

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    public RestaurantEsewa(String restaurantCode, String productCode,
                           String encryptedSecretKey, boolean enabled) {
        this.restaurantCode = restaurantCode;
        this.productCode = productCode;
        this.secretKey = encryptedSecretKey;
        this.enabled = enabled;
    }

    /** Replace the stored credentials. The secret key must already be encrypted. */
    public void update(String productCode, String encryptedSecretKey, boolean enabled) {
        this.productCode = productCode;
        this.secretKey = encryptedSecretKey;
        this.enabled = enabled;
    }
}
