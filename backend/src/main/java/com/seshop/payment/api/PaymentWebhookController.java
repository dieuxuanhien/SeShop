package com.seshop.payment.api;

import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.application.OrderService;
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
    private static final String ORDER_STATUS_PAID = "PAID";
    private static final String ORDER_STATUS_PAYMENT_FAILED = "PAYMENT_FAILED";
    private static final String ORDER_STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final String PAYMENT_STATUS_FAILED = "FAILED";
    private static final String PAYMENT_STATUS_PAID = "PAID";

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final StripeProperties stripeProperties;

    public PaymentWebhookController(
            PaymentRepository paymentRepository,
            OrderService orderService,
            StripeProperties stripeProperties
    ) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
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
            paymentIntent(event).ifPresent(this::handlePaymentSucceeded);
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            paymentIntent(event).ifPresent(this::handlePaymentFailed);
        }

        return ResponseEntity.ok(Map.of("data", Map.of("accepted", true)));
    }

    private java.util.Optional<PaymentIntent> paymentIntent(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        return dataObjectDeserializer.getObject()
                .filter(PaymentIntent.class::isInstance)
                .map(PaymentIntent.class::cast);
    }

    private void handlePaymentSucceeded(PaymentIntent paymentIntent) {
        String transactionId = paymentIntent.getId();
        log.info("Handling successful payment for transaction: {}", transactionId);

        paymentRepository.findByTransactionId(transactionId)
                .ifPresent(payment -> {
                    OrderEntity order = payment.getOrder();
                    if (ORDER_STATUS_PAYMENT_FAILED.equalsIgnoreCase(order.getStatus())) {
                        log.warn("Ignoring late successful Stripe event for failed order {}", order.getOrderNumber());
                        return;
                    }
                    if (!ORDER_STATUS_PENDING_PAYMENT.equalsIgnoreCase(order.getStatus())
                            && !ORDER_STATUS_PAID.equalsIgnoreCase(order.getStatus())) {
                        log.warn("Ignoring Stripe success for order {} in status {}", order.getOrderNumber(), order.getStatus());
                        return;
                    }

                    payment.setStatus(PAYMENT_STATUS_PAID);
                    paymentRepository.save(payment);
                    orderService.markPaymentPaid(order.getId());
                    log.info("Order {} marked as paid", order.getOrderNumber());
                });
    }

    private void handlePaymentFailed(PaymentIntent paymentIntent) {
        String transactionId = paymentIntent.getId();
        log.info("Handling failed payment for transaction: {}", transactionId);

        paymentRepository.findByTransactionId(transactionId)
                .ifPresent(payment -> {
                    OrderEntity order = payment.getOrder();
                    if (PAYMENT_STATUS_PAID.equalsIgnoreCase(payment.getStatus())
                            || ORDER_STATUS_PAID.equalsIgnoreCase(order.getStatus())) {
                        log.warn("Ignoring Stripe failure for already-paid order {}", order.getOrderNumber());
                        return;
                    }
                    if (!ORDER_STATUS_PENDING_PAYMENT.equalsIgnoreCase(order.getStatus())) {
                        log.warn("Ignoring Stripe failure for order {} in status {}", order.getOrderNumber(), order.getStatus());
                        return;
                    }

                    payment.setStatus(PAYMENT_STATUS_FAILED);
                    paymentRepository.save(payment);
                    orderService.markPaymentFailed(order.getId());
                    log.info("Order {} marked as PAYMENT_FAILED", order.getOrderNumber());
                });
    }
}
