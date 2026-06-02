package com.restaurant.api.gateway.esewa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Thin HTTP client for eSewa's transaction status API — the secondary
 * confirmation that a transaction actually settled. Isolated so the gateway's
 * signing/verification logic stays free of network concerns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsewaClient {

    /**
     * Outcome of a status query. {@link #UNAVAILABLE} means we could not reach
     * eSewa (DNS/network/downtime) — distinct from a definitive NOT_COMPLETE —
     * so the caller can decide how much to trust the signed callback instead.
     */
    public enum StatusResult {COMPLETE, NOT_COMPLETE, UNAVAILABLE}

    private final EsewaProperties properties;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Query eSewa for the settled status of a transaction. Never throws. */
    public StatusResult checkStatus(String transactionUuid, String totalAmount) {
        String url = properties.getStatusUrl()
                + "?product_code=" + enc(properties.getProductCode())
                + "&total_amount=" + enc(totalAmount)
                + "&transaction_uuid=" + enc(transactionUuid);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("eSewa status check HTTP {} for {}", response.statusCode(), transactionUuid);
                return StatusResult.UNAVAILABLE;
            }
            JsonNode body = objectMapper.readTree(response.body());
            return "COMPLETE".equalsIgnoreCase(body.path("status").asText())
                    ? StatusResult.COMPLETE
                    : StatusResult.NOT_COMPLETE;
        } catch (IOException e) {
            log.warn("eSewa status check unreachable for {}: {}", transactionUuid, e.toString());
            return StatusResult.UNAVAILABLE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("eSewa status check interrupted for {}", transactionUuid);
            return StatusResult.UNAVAILABLE;
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
