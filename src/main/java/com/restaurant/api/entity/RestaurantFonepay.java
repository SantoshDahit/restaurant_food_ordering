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
 * A restaurant's Fonepay merchant credentials (one row per restaurant, keyed by
 * restaurant code). Lives in its own domain/table so payment-gateway secrets
 * don't bloat the core Restaurant entity.
 *
 * <p>{@code username}, {@code password} and {@code secretKey} are stored
 * ENCRYPTED — the service encrypts them via CredentialCipher before persisting,
 * so this entity never holds plaintext secrets in the DB.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "restaurant_fonepay")
@EntityListeners(AuditingEntityListener.class)
public class RestaurantFonepay extends BaseTimeEntity {

    @Id
    @Column(name = "restaurant_code")
    private String restaurantCode;

    @Column(name = "merchant_code", nullable = false)
    private String merchantCode;

    @Column(name = "username", nullable = false, length = 512)
    private String username;     // encrypted

    @Column(name = "password", nullable = false, length = 512)
    private String password;     // encrypted

    @Column(name = "secret_key", nullable = false, length = 512)
    private String secretKey;    // encrypted

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    public RestaurantFonepay(String restaurantCode, String merchantCode, String encryptedUsername,
                             String encryptedPassword, String encryptedSecretKey, boolean enabled) {
        this.restaurantCode = restaurantCode;
        this.merchantCode = merchantCode;
        this.username = encryptedUsername;
        this.password = encryptedPassword;
        this.secretKey = encryptedSecretKey;
        this.enabled = enabled;
    }

    /** Replace the stored credentials. Sensitive values must already be encrypted. */
    public void update(String merchantCode, String encryptedUsername,
                       String encryptedPassword, String encryptedSecretKey, boolean enabled) {
        this.merchantCode = merchantCode;
        this.username = encryptedUsername;
        this.password = encryptedPassword;
        this.secretKey = encryptedSecretKey;
        this.enabled = enabled;
    }
}
