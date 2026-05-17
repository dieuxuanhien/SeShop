package com.seshop.shipping.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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

        String response;
        try {
            response = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .build()
                    .post()
                    .uri(properties.getCreateOrderPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Token", properties.getToken())
                    .header("ShopId", properties.getShopId())
                    .body(buildCreateOrderBody(orderNumber, toName, toPhone, toAddress))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            throw new BusinessException("ORD_002", parseGhnError(exception.getResponseBodyAsString()));
        }

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
        String response;
        try {
            response = RestClient.builder()
                    .baseUrl(properties.getBaseUrl())
                    .build()
                    .post()
                    .uri(properties.getTrackPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Token", properties.getToken())
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            throw new BusinessException("ORD_002", parseGhnError(exception.getResponseBodyAsString()));
        }

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

    private Map<String, Object> buildCreateOrderBody(String orderNumber, String toName, String toPhone, String toAddress) {
        AddressParts address = AddressParts.from(toAddress);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_order_code", orderNumber);
        body.put("from_name", properties.getDefaultFromName());
        body.put("from_phone", properties.getDefaultFromPhone());
        body.put("from_address", properties.getDefaultFromAddress());
        body.put("from_ward_name", properties.getDefaultFromWardName());
        body.put("from_district_name", properties.getDefaultFromDistrictName());
        body.put("from_province_name", properties.getDefaultFromProvinceName());
        body.put("return_phone", properties.getDefaultFromPhone());
        body.put("return_address", properties.getDefaultFromAddress());
        body.put("return_ward_code", properties.getDefaultToWardCode());
        body.put("return_district_id", properties.getDefaultToDistrictId());
        body.put("to_name", toName);
        body.put("to_phone", toPhone);
        body.put("to_address", StringUtils.hasText(properties.getDefaultToAddress())
                ? properties.getDefaultToAddress()
                : address.line1());
        if (StringUtils.hasText(properties.getDefaultToWardCode()) && properties.getDefaultToDistrictId() > 0) {
            body.put("to_ward_code", properties.getDefaultToWardCode());
            body.put("to_district_id", properties.getDefaultToDistrictId());
        } else {
            body.put("to_ward_name", address.ward());
            body.put("to_district_name", address.district());
            body.put("to_province_name", address.province());
        }
        body.put("required_note", properties.getRequiredNote());
        body.put("payment_type_id", properties.getPaymentTypeId());
        body.put("service_type_id", properties.getServiceTypeId());
        body.put("weight", properties.getDefaultWeightGrams());
        body.put("length", properties.getDefaultLengthCm());
        body.put("width", properties.getDefaultWidthCm());
        body.put("height", properties.getDefaultHeightCm());
        body.put("cod_amount", properties.getDefaultCodAmount());
        body.put("insurance_value", properties.getDefaultInsuranceValue());
        body.put("content", properties.getDefaultContent() + " " + orderNumber);
        body.put("items", List.of(Map.of(
                "name", properties.getDefaultContent(),
                "code", orderNumber,
                "quantity", 1,
                "price", properties.getDefaultInsuranceValue(),
                "length", properties.getDefaultLengthCm(),
                "width", properties.getDefaultWidthCm(),
                "height", properties.getDefaultHeightCm(),
                "weight", properties.getDefaultWeightGrams()
        )));
        return body;
    }

    private String parseGhnError(String responseBody) {
        try {
            Map<String, Object> payload = objectMapper.readValue(responseBody, new TypeReference<>() {});
            String message = asString(payload.get("message"));
            String codeMessage = asString(payload.get("code_message_value"));
            if (StringUtils.hasText(message) && StringUtils.hasText(codeMessage)) {
                return "GHN request failed: " + message + " (" + codeMessage + ")";
            }
            if (StringUtils.hasText(message)) {
                return "GHN request failed: " + message;
            }
        } catch (Exception ignored) {
            // Fall through to a generic integration error.
        }
        return "GHN request failed";
    }

    private String asString(Object value) {
        return value instanceof String text ? text : null;
    }

    private record AddressParts(String line1, String ward, String district, String province) {
        static AddressParts from(String rawAddress) {
            if (!StringUtils.hasText(rawAddress)) {
                return new AddressParts("", "", "", "");
            }
            String[] parts = rawAddress.split("\\s*,\\s*");
            String line1 = parts.length >= 3 ? parts[2] : rawAddress;
            String ward = parts.length >= 4 ? normalizeWard(parts[3]) : "";
            String district = parts.length >= 5 ? normalizeDistrict(parts[4]) : "";
            String province = parts.length >= 6 ? normalizeProvince(parts[5]) : "";
            return new AddressParts(line1, ward, district, province);
        }

        private static String normalizeWard(String ward) {
            if (!StringUtils.hasText(ward)) {
                return "";
            }
            String trimmed = ward.trim();
            return trimmed.matches("\\d+") ? "Phường " + trimmed : trimmed;
        }

        private static String normalizeDistrict(String district) {
            if (!StringUtils.hasText(district)) {
                return "";
            }
            String trimmed = district.trim();
            return trimmed.matches("\\d+") ? "Quận " + trimmed : trimmed;
        }

        private static String normalizeProvince(String province) {
            if (!StringUtils.hasText(province)) {
                return "";
            }
            String trimmed = province.trim();
            return trimmed.equalsIgnoreCase("Ho Chi Minh City") ? "HCM" : trimmed;
        }
    }

    public record GhnShipmentResult(String trackingNumber, String status) {
    }
}
