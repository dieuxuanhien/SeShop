package com.seshop.shipping.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "seshop.integrations.ghn")
public class GhnProperties {

    private boolean enabled = false;
    private String baseUrl = "https://dev-online-gateway.ghn.vn";
    private String token;
    private String shopId;
    private String createOrderPath = "/shiip/public-api/v2/shipping-order/create";
    private String trackPath = "/shiip/public-api/v2/shipping-order/detail";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getCreateOrderPath() {
        return createOrderPath;
    }

    public void setCreateOrderPath(String createOrderPath) {
        this.createOrderPath = createOrderPath;
    }

    public String getTrackPath() {
        return trackPath;
    }

    public void setTrackPath(String trackPath) {
        this.trackPath = trackPath;
    }
}
