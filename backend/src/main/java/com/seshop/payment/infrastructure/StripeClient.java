package com.seshop.payment.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class StripeClient {

    private final StripeProperties properties;
    private final ObjectMapper objectMapper;

    public StripeClient(StripeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public StripePaymentResult createPaymentIntent(BigDecimal amount, String idempotencyKey, String orderNumber) {
        if (!properties.isEnabled()) {
            throw new BusinessException("PAY_002", "Stripe integration is disabled");
        }
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new BusinessException("PAY_002", "Stripe secret key is not configured");
        }

        long amountInMinorUnits = amount.multiply(BigDecimal.valueOf(100L)).longValue();
        String form = "amount=" + encode(String.valueOf(amountInMinorUnits))
                + "&currency=" + encode(properties.getCurrency())
                + "&metadata[order_number]=" + encode(orderNumber)
                + "&automatic_payment_methods[enabled]=true";

        String response = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build()
                .post()
                .uri("/payment_intents")
                .header("Authorization", "Bearer " + properties.getSecretKey())
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            String transactionId = (String) payload.get("id");
            String status = (String) payload.getOrDefault("status", "requires_payment_method");
            String clientSecret = (String) payload.get("client_secret");
            return new StripePaymentResult(transactionId, status, clientSecret);
        } catch (Exception exception) {
            throw new BusinessException("PAY_001", "Cannot parse Stripe response");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record StripePaymentResult(String transactionId, String status, String clientSecret) {
    }
}
