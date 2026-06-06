package com.restaurant.api.gateway.esewa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.api.exception.ApiException;
import com.restaurant.api.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * eSewa ePay v2 gateway. Builds the signed payment form and verifies the
 * signed redirect-back payload (response signature + authoritative status
 * check). Deals only in primitives and {@link EsewaFormData}/
 * {@link EsewaVerification} — it has no knowledge of the Payment domain, so
 * the only place that ties eSewa to a payment is {@code PaymentService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsewaGateway {

    private static final String SIGNED_FIELDS = "total_amount,transaction_uuid,product_code";

    private final EsewaProperties properties;
    private final EsewaClient client;
    private final ObjectMapper objectMapper;

    /**
     * Build the signed form fields the browser submits to eSewa's hosted page,
     * signed with the specific restaurant's merchant credentials. No tax or
     * service/delivery charges, so amount == total_amount.
     *
     * @param creds           the restaurant's eSewa merchant credentials
     * @param transactionUuid merchant transaction id (the caller's payment code)
     */
    public EsewaFormData initiate(EsewaCredentials creds, String transactionUuid, BigDecimal amount,
                                  String successUrl, String failureUrl) {
        String totalAmount = amount.stripTrailingZeros().toPlainString();
        String message = "total_amount=" + totalAmount
                + ",transaction_uuid=" + transactionUuid
                + ",product_code=" + creds.productCode();

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("amount", totalAmount);
        fields.put("tax_amount", "0");
        fields.put("total_amount", totalAmount);
        fields.put("transaction_uuid", transactionUuid);
        fields.put("product_code", creds.productCode());
        fields.put("product_service_charge", "0");
        fields.put("product_delivery_charge", "0");
        fields.put("success_url", successUrl);
        fields.put("failure_url", failureUrl);
        fields.put("signed_field_names", SIGNED_FIELDS);
        fields.put("signature", EsewaSignatureUtil.sign(creds.secretKey(), message));

        return new EsewaFormData(properties.getFormUrl(), fields);
    }

    /**
     * Decode an eSewa redirect-back payload just far enough to read the
     * {@code transaction_uuid} (= our payment code) WITHOUT verifying — the
     * caller uses it to resolve which restaurant's credentials to verify with.
     */
    public String peekTransactionUuid(String base64Data) {
        String uuid = decode(base64Data).path("transaction_uuid").asText(null);
        if (uuid == null) {
            throw new ApiException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }
        return uuid;
    }

    /**
     * Decode + verify eSewa's redirect-back payload using the restaurant's
     * credentials: validate the response signature locally, then confirm
     * COMPLETE against the status API. Throws {@link ApiException} when the
     * payload is malformed or tampered.
     */
    public EsewaVerification verify(EsewaCredentials creds, String base64Data) {
        JsonNode node = decode(base64Data);

        String transactionUuid = node.path("transaction_uuid").asText(null);
        String totalAmount = node.path("total_amount").asText(null);
        if (transactionUuid == null || totalAmount == null) {
            throw new ApiException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }
        if (!hasValidSignature(creds, node)) {
            log.warn("eSewa: signature mismatch for transaction_uuid={}", transactionUuid);
            throw new ApiException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        // Callback total_amount can carry thousands separators ("1,000.0").
        String normalizedAmount = totalAmount.replace(",", "");
        boolean signedComplete = "COMPLETE".equalsIgnoreCase(node.path("status").asText());

        // The signed payload already proves authenticity; the status API is a
        // secondary defence-in-depth check. If eSewa is unreachable, fall back
        // to the signed status rather than failing a genuine payment.
        EsewaClient.StatusResult status = client.checkStatus(creds.productCode(), transactionUuid, normalizedAmount);
        boolean success = switch (status) {
            case COMPLETE -> signedComplete;
            case NOT_COMPLETE -> false;
            case UNAVAILABLE -> {
                log.warn("eSewa status API unavailable for {}; trusting signed callback (status={})",
                        transactionUuid, signedComplete ? "COMPLETE" : "incomplete");
                yield signedComplete;
            }
        };

        return new EsewaVerification(
                success,
                transactionUuid,
                node.path("transaction_code").asText(null),
                new BigDecimal(normalizedAmount)
        );
    }

    private JsonNode decode(String base64Data) {
        try {
            String json = new String(Base64.getDecoder().decode(base64Data), StandardCharsets.UTF_8);
            return objectMapper.readTree(json);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            log.warn("eSewa: failed to decode callback data", e);
            throw new ApiException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }
    }

    /** Recompute the signature over eSewa's own declared signed fields and compare. */
    private boolean hasValidSignature(EsewaCredentials creds, JsonNode node) {
        String signedFieldNames = node.path("signed_field_names").asText(null);
        String signature = node.path("signature").asText(null);
        if (signedFieldNames == null || signature == null) {
            return false;
        }
        StringBuilder message = new StringBuilder();
        String[] names = signedFieldNames.split(",");
        for (int i = 0; i < names.length; i++) {
            if (i > 0) message.append(",");
            String name = names[i].trim();
            message.append(name).append("=").append(node.path(name).asText(""));
        }
        return EsewaSignatureUtil.matches(creds.secretKey(), message.toString(), signature);
    }
}
