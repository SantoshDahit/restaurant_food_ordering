package com.restaurant.api.gateway.fonepay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Fonepay dynamic-QR environment configuration. Merchant credentials are
 * per-restaurant (stored encrypted); only the environment URLs are global,
 * switched with {@code payment.fonepay.mode} (TEST = UAT, PRODUCTION = live).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.fonepay")
public class FonepayProperties {

    public enum Mode {TEST, PRODUCTION}

    private Mode mode = Mode.TEST;
    private String testBaseUrl = "https://uat-new-merchant-api.fonepay.com";
    private String productionBaseUrl = "https://merchantapi.fonepay.com";

    private String baseUrl() {
        return mode == Mode.PRODUCTION ? productionBaseUrl : testBaseUrl;
    }

    public String qrDownloadUrl() {
        return baseUrl() + "/api/merchant/merchantDetailsForThirdParty/thirdPartyDynamicQrDownload";
    }

    public String qrStatusUrl() {
        return baseUrl() + "/api/merchant/merchantDetailsForThirdParty/thirdPartyDynamicQrGetStatus";
    }
}
