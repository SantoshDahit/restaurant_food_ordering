package com.restaurant.api.gateway.fonepay;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Interprets Fonepay status/notification payloads into a verdict. Kept separate
 * from the gateway/transport so the "is this payment real?" decision lives in
 * one auditable place.
 */
@Slf4j
@Component
public class FonepayVerifier {

    private static final String SETTLED = "success";

    /**
     * Verdict from a {@code thirdPartyDynamicQrGetStatus} response. A payment is
     * only treated as settled when Fonepay reports success AND the returned PRN
     * matches the one we queried (guards against mismatched/replayed responses).
     */
    public FonepayVerification interpret(JsonNode statusResponse, String expectedPrn) {
        String prn = statusResponse.path("prn").asText(null);
        String paymentStatus = statusResponse.path("paymentStatus").asText(null);
        String traceId = statusResponse.path("fonepayTraceId").asText(null);

        boolean prnMatches = expectedPrn != null && expectedPrn.equals(prn);
        if (!prnMatches) {
            log.warn("Fonepay status PRN mismatch: expected {}, got {}", expectedPrn, prn);
        }
        boolean success = prnMatches && SETTLED.equalsIgnoreCase(paymentStatus);

        return new FonepayVerification(success, prn != null ? prn : expectedPrn, traceId, paymentStatus);
    }
}
