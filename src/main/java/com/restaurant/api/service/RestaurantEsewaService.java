package com.restaurant.api.service;

import com.restaurant.api.dto.RestaurantDto;
import com.restaurant.api.entity.RestaurantEsewa;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import com.restaurant.api.gateway.esewa.EsewaCredentials;
import com.restaurant.api.repository.restaurantesewa.RestaurantEsewaRepository;
import com.restaurant.api.security.CredentialCipher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Owns a restaurant's eSewa credentials: stores the secret key encrypted at rest
 * and hands back decrypted credentials only to the gateway. Mirrors
 * {@link RestaurantFonepayService}.
 */
@Service
@RequiredArgsConstructor
public class RestaurantEsewaService {
    private final RestaurantEsewaRepository repository;
    private final CredentialCipher cipher;

    /** Upsert the credentials for a restaurant, encrypting the secret key. */
    @Transactional
    public RestaurantEsewa save(String restaurantCode, RestaurantDto.EsewaCredentialsRequest request) {
        String encSecret = cipher.encrypt(request.secretKey());

        RestaurantEsewa entity = repository.findByRestaurantCode(restaurantCode)
                .map(existing -> {
                    existing.update(request.productCode(), encSecret, request.enabled());
                    return existing;
                })
                .orElseGet(() -> new RestaurantEsewa(
                        restaurantCode, request.productCode(), encSecret, request.enabled()));
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<RestaurantEsewa> find(String restaurantCode) {
        return repository.findByRestaurantCode(restaurantCode);
    }

    /** Decrypted credentials for the gateway. Throws if not configured. */
    @Transactional(readOnly = true)
    public EsewaCredentials getDecrypted(String restaurantCode) {
        RestaurantEsewa entity = repository.findByRestaurantCode(restaurantCode)
                .orElseThrow(() -> new ApiException(ErrorCode.ESEWA_NOT_CONFIGURED));
        return new EsewaCredentials(entity.getProductCode(), cipher.decrypt(entity.getSecretKey()));
    }
}
