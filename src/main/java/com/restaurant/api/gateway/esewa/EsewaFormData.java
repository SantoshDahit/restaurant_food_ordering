package com.restaurant.api.gateway.esewa;

import java.util.Map;

/**
 * The target URL and signed field set the browser POSTs to eSewa's hosted
 * payment page. A plain gateway result — no payment-domain coupling.
 */
public record EsewaFormData(String formUrl, Map<String, String> fields) {
}
