package com.seshop.shipping.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class GhnClient {

    private final GhnProperties properties;
    private final ObjectMapper objectMapper;

    public GhnClient(GhnProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public GhnShipmentResult createShippingOrder(String orderNumber, String toName, String toPhone, String toAddress) {
        if (!properties.isEnabled()) {
            throw new BusinessException("ORD_002", "GHN integration is disabled");
        }
        ensureConfigured();

        Map<String, Object> body = Map.of(
                "client_order_code", orderNumber,
                "to_name", toName,
                "to_phone", toPhone,
                "to_address", toAddress,
                "required_note", "KHONGCHOXEMHANG"
        );

        String response = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri(properties.getCreateOrderPath())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", properties.getToken())
                .header("ShopId", properties.getShopId())
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
            String trackingNumber = (String) data.getOrDefault("order_code", "");
            String status = (String) data.getOrDefault("status", "ready_to_pick");
            if (!StringUtils.hasText(trackingNumber)) {
                throw new BusinessException("ORD_002", "GHN did not return tracking number");
            }
            return new GhnShipmentResult(trackingNumber, status);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("ORD_002", "Cannot parse GHN response");
        }
    }

    public String getShippingStatus(String trackingNumber) {
        if (!properties.isEnabled()) {
            return "PENDING";
        }
        ensureConfigured();

        Map<String, Object> body = Map.of("order_code", trackingNumber);
        String response = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri(properties.getTrackPath())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Token", properties.getToken())
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
            return (String) data.getOrDefault("status", "PENDING");
        } catch (Exception exception) {
            throw new BusinessException("ORD_002", "Cannot parse GHN tracking response");
        }
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(properties.getToken()) || !StringUtils.hasText(properties.getShopId())) {
            throw new BusinessException("ORD_002", "GHN token or shop id is not configured");
        }
    }

    public record GhnShipmentResult(String trackingNumber, String status) {
    }
}
