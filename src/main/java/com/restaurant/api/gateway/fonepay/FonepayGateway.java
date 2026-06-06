package com.restaurant.api.gateway.fonepay;

import com.fasterxml.jackson.databind.JsonNode;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fonepay dynamic-QR gateway. Generates a signed QR-download request and checks
 * settled status, each call signed with the specific restaurant's secret. Deals
 * only in primitives + {@link FonepayCredentials} / {@link FonepayQr} /
 * {@link FonepayVerification} — it has no knowledge of orders or restaurants, so
 * the only place that ties Fonepay to a payment is the payment orchestration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FonepayGateway {

    private final FonepayProperties properties;
    private final FonepayClient client;
    private final FonepayVerifier verifier;

    /**
     * Download a dynamic QR for an exact amount. {@code prn} is the unique
     * merchant transaction reference (e.g. {@code SAAS-<restaurant>-<order>-<n>}).
     */
    public FonepayQr generateQr(FonepayCredentials creds, BigDecimal amount, String prn,
                                String remarks1, String remarks2) {
        String amountStr = amount.stripTrailingZeros().toPlainString();
        String message = String.join(",", amountStr, prn, creds.merchantCode(), remarks1, remarks2);
        String signature = FonepaySignatureUtil.sign(creds.secretKey(), message);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amountStr);
        body.put("remarks1", remarks1);
        body.put("remarks2", remarks2);
        body.put("prn", prn);
        body.put("merchantCode", creds.merchantCode());
        body.put("dataValidation", signature);
        body.put("username", creds.username());
        body.put("password", creds.password());

        JsonNode response = client.post(properties.qrDownloadUrl(), body);
        if (!response.path("success").asBoolean(false)) {
            log.warn("Fonepay QR download failed for prn={}: {}", prn, response.path("message").asText(""));
            throw new ApiException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
        return new FonepayQr(
                response.path("qrMessage").asText(null),
                response.path("thirdpartyQrWebSocketUrl").asText(null),
                prn);
    }

    /** Check the settled status of a transaction by PRN (the authoritative confirmation). */
    public FonepayVerification verify(FonepayCredentials creds, String prn) {
        String message = String.join(",", prn, creds.merchantCode());
        String signature = FonepaySignatureUtil.sign(creds.secretKey(), message);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("prn", prn);
        body.put("merchantCode", creds.merchantCode());
        body.put("dataValidation", signature);
        body.put("username", creds.username());
        body.put("password", creds.password());

        JsonNode response = client.post(properties.qrStatusUrl(), body);
        return verifier.interpret(response, prn);
    }
}
