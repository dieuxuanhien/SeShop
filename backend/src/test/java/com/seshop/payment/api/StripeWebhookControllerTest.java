package com.seshop.payment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.infrastructure.persistence.OrderRepository;
import com.seshop.commerce.infrastructure.persistence.PaymentEntity;
import com.seshop.commerce.infrastructure.persistence.PaymentRepository;
import com.seshop.payment.infrastructure.StripeProperties;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Stripe Webhook integration tests.
 * With signature verification enabled, only signed Stripe events are processed.
 * UC15 (Payment) + UC16 (Order Status Update) verification path.
 */
@WebMvcTest(controllers = PaymentWebhookController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "seshop.cors.allowed-origins=http://localhost:5173",
        "seshop.integrations.stripe.enabled=true",
        "seshop.integrations.stripe.webhook-secret=whsec_test_secret"
})
class StripeWebhookControllerTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // SpyBean so real StripeProperties is wired (enabled=false via TestPropertySource)
    @SpyBean
    private StripeProperties stripeProperties;

    @Test
    void webhookRejectsMissingSignatureWhenVerificationEnabled() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stripePayload("evt_test_001", "payment_intent.succeeded", "pi_test_001", "succeeded")))
                .andExpect(status().isForbidden());
    }

    @Test
    void webhookUpdatesPaymentStatusToCompleted() throws Exception {
        OrderEntity order = new OrderEntity();
        order.setId(99L);
        order.setOrderNumber("ORD-99");
        order.setStatus("PENDING_PAYMENT");

        PaymentEntity payment = new PaymentEntity();
        payment.setId(1L);
        payment.setTransactionId("pi_test_002");
        payment.setOrder(order);
        payment.setStatus("PENDING");

        given(paymentRepository.findByTransactionId("pi_test_002")).willReturn(Optional.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        given(orderRepository.save(order)).willReturn(order);

        String payload = stripePayload("evt_test_002", "payment_intent.succeeded", "pi_test_002", "succeeded");

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook-status")
                        .header("Stripe-Signature", stripeSignature(payload))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        assertThat(payment.getStatus()).isEqualTo("COMPLETED");
        assertThat(order.getStatus()).isEqualTo("PAID");
    }

    @Test
    void webhookSetsPaymentToPendingForNonSucceededStatus() throws Exception {
        String payload = stripePayload("evt_test_003", "payment_intent.payment_failed", "pi_test_003", "requires_payment_method");

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook-fail")
                        .header("Stripe-Signature", stripeSignature(payload))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void webhookIgnoresUnknownTransactionId() throws Exception {
        given(paymentRepository.findByTransactionId("pi_not_found")).willReturn(Optional.empty());

        String payload = stripePayload("evt_test_004", "payment_intent.succeeded", "pi_not_found", "succeeded");

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook-noop")
                        .header("Stripe-Signature", stripeSignature(payload))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        then(paymentRepository).should().findByTransactionId("pi_not_found");
    }

    private String stripePayload(String eventId, String eventType, String paymentIntentId, String status) {
        return """
                {
                  "id": "%s",
                  "object": "event",
                  "api_version": "2024-04-10",
                  "type": "%s",
                  "data": {
                    "object": {
                      "id": "%s",
                      "object": "payment_intent",
                      "status": "%s"
                    }
                  }
                }
                """.formatted(eventId, eventType, paymentIntentId, status);
    }

    private String stripeSignature(String payload) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String signedPayload = timestamp + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8)));
        return "t=" + timestamp + ",v1=" + signature;
    }
}
