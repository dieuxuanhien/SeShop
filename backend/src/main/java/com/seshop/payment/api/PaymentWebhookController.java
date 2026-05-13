package com.seshop.payment.api;

import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.infrastructure.persistence.OrderRepository;
import com.seshop.commerce.infrastructure.persistence.PaymentEntity;
import com.seshop.commerce.infrastructure.persistence.PaymentRepository;
import com.seshop.payment.infrastructure.StripeProperties;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/payments")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripeProperties stripeProperties;

    public PaymentWebhookController(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            StripeProperties stripeProperties
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.stripeProperties = stripeProperties;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleStripeEvent(
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestBody String payload
    ) {
        Event event;

        if (stripeProperties.isEnabled() && stripeProperties.getWebhookSecret() != null
                && !stripeProperties.getWebhookSecret().isBlank()) {
            if (signature == null || signature.isBlank()) {
                log.warn("Missing Stripe signature");
                throw new ForbiddenOperationException("Missing Stripe signature");
            }
            try {
                event = Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
            } catch (SignatureVerificationException e) {
                log.warn("Invalid Stripe signature: {}", e.getMessage());
                throw new ForbiddenOperationException("Invalid Stripe signature");
            }
        } else {
            // For testing without secret verification
            log.warn("Webhook signature verification is disabled");
            return ResponseEntity.ok(Map.of("data", Map.of("accepted", true, "message", "Ignored due to missing secret")));
        }

        log.info("Received Stripe event: {} (id: {})", event.getType(), event.getId());

        if ("payment_intent.succeeded".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                PaymentIntent paymentIntent = (PaymentIntent) dataObjectDeserializer.getObject().get();
                handlePaymentSucceeded(paymentIntent);
            }
        }

        return ResponseEntity.ok(Map.of("data", Map.of("accepted", true)));
    }

    private void handlePaymentSucceeded(PaymentIntent paymentIntent) {
        String transactionId = paymentIntent.getId();
        log.info("Handling successful payment for transaction: {}", transactionId);

        paymentRepository.findByTransactionId(transactionId)
                .ifPresent(payment -> {
                    payment.setStatus("COMPLETED");
                    paymentRepository.save(payment);

                    OrderEntity order = payment.getOrder();
                    if ("PENDING_PAYMENT".equalsIgnoreCase(order.getStatus())) {
                        order.setStatus("PAID");
                        orderRepository.save(order);
                        log.info("Order {} updated to PAID", order.getOrderNumber());
                    }
                });
    }
}
