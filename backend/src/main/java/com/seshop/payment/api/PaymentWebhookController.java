package com.seshop.payment.api;

import com.seshop.commerce.infrastructure.persistence.PaymentEntity;
import com.seshop.commerce.infrastructure.persistence.PaymentRepository;
import com.seshop.payment.infrastructure.StripeProperties;
import com.seshop.shared.exception.ForbiddenOperationException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/payments")
public class PaymentWebhookController {

    private final PaymentRepository paymentRepository;
    private final StripeProperties stripeProperties;

    public PaymentWebhookController(PaymentRepository paymentRepository, StripeProperties stripeProperties) {
        this.paymentRepository = paymentRepository;
        this.stripeProperties = stripeProperties;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleStripeEvent(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody Map<String, Object> payload
    ) {
        if (stripeProperties.isEnabled() && stripeProperties.getWebhookSecret() != null
                && !stripeProperties.getWebhookSecret().isBlank()) {
            if (signature == null || signature.isBlank()) {
                throw new ForbiddenOperationException("Missing Stripe signature");
            }
        }

        Map<String, Object> data = (Map<String, Object>) payload.getOrDefault("data", Map.of());
        Map<String, Object> object = (Map<String, Object>) data.getOrDefault("object", Map.of());
        String transactionId = (String) object.get("id");
        String status = (String) object.get("status");

        if (transactionId != null) {
            List<PaymentEntity> payments = paymentRepository.findAll().stream()
                    .filter(item -> transactionId.equals(item.getTransactionId()))
                    .toList();
            for (PaymentEntity payment : payments) {
                payment.setStatus("succeeded".equalsIgnoreCase(status) ? "COMPLETED" : "PENDING");
                paymentRepository.save(payment);
            }
        }

        return ResponseEntity.ok(Map.of("data", Map.of("accepted", true)));
    }
}
