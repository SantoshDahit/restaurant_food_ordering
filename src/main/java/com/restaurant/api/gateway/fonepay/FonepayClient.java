package com.restaurant.api.gateway.fonepay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Thin HTTP client for Fonepay's merchant API — JSON POST in, parsed JSON out.
 * Isolated so the gateway's signing/verification logic stays free of transport
 * concerns.
 */
@Slf4j
@Component
public class FonepayClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public FonepayClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** POST a JSON body and return the parsed response. Throws on transport/HTTP errors. */
    public JsonNode post(String url, Map<String, Object> body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Fonepay HTTP {} from {}", response.statusCode(), url);
                throw new ApiException(ErrorCode.PAYMENT_GATEWAY_ERROR);
            }
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            log.error("Fonepay request to {} failed", url, e);
            throw new ApiException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }
}
