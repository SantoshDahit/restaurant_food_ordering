package com.restaurant.api.service;

import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.RestaurantFonepay;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.gateway.fonepay.FonepayCredentials;
import com.restaurant.api.repository.restaurantfonepay.RestaurantFonepayRepository;
import com.restaurant.api.security.CredentialCipher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Owns a restaurant's Fonepay credentials: stores the sensitive values
 * encrypted at rest and hands back decrypted credentials only to the gateway.
 */
@Service
@RequiredArgsConstructor
public class RestaurantFonepayService {
    private final RestaurantFonepayRepository repository;
    private final CredentialCipher cipher;

    /** Upsert the credentials for a restaurant, encrypting the sensitive values. */
    @Transactional
    public RestaurantFonepay save(String restaurantCode, RestaurantDto.FonepayCredentialsRequest request) {
        String encUsername = cipher.encrypt(request.username());
        String encPassword = cipher.encrypt(request.password());
        String encSecret = cipher.encrypt(request.secretKey());

        RestaurantFonepay entity = repository.findByRestaurantCode(restaurantCode)
                .map(existing -> {
                    existing.update(request.merchantCode(), encUsername, encPassword, encSecret, request.enabled());
                    return existing;
                })
                .orElseGet(() -> new RestaurantFonepay(
                        restaurantCode, request.merchantCode(), encUsername, encPassword, encSecret, request.enabled()));
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantFonepay> find(String restaurantCode) {
        return repository.findByRestaurantCode(restaurantCode);
    }

    /** Decrypted credentials for the gateway. Throws if not configured. */
    @Transactional(readOnly = true)
    public FonepayCredentials getDecrypted(String restaurantCode) {
        RestaurantFonepay entity = repository.findByRestaurantCode(restaurantCode)
                .orElseThrow(() -> new ApiException(ErrorCode.FONEPAY_NOT_CONFIGURED));
        return new FonepayCredentials(
                entity.getMerchantCode(),
                cipher.decrypt(entity.getUsername()),
                cipher.decrypt(entity.getPassword()),
                cipher.decrypt(entity.getSecretKey()));
    }
}
