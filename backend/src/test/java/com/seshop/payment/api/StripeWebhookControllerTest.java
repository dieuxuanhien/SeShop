package com.seshop.payment.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
 * With stripe.enabled=false the controller accepts webhooks without signature verification,
 * matching the local/test environment security posture.
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
        "seshop.integrations.stripe.enabled=false"
})
class StripeWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // SpyBean so real StripeProperties is wired (enabled=false via TestPropertySource)
    @SpyBean
    private StripeProperties stripeProperties;

    @Test
    void webhookAcceptedWithoutSignatureWhenDisabled() throws Exception {
        given(paymentRepository.findAll()).willReturn(List.of());

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "payment_intent.succeeded",
                                  "data": {
                                    "object": {
                                      "id": "pi_test_001",
                                      "status": "succeeded"
                                    }
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accepted").value(true));
    }

    @Test
    void webhookUpdatesPaymentStatusToCompleted() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(1L);
        payment.setTransactionId("pi_test_002");
        payment.setAmount(new BigDecimal("590000"));
        payment.setStatus("PENDING");

        given(paymentRepository.findAll()).willReturn(List.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "payment_intent.succeeded",
                                  "data": {
                                    "object": {
                                      "id": "pi_test_002",
                                      "status": "succeeded"
                                    }
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        assertThat(payment.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void webhookSetsPaymentToPendingForNonSucceededStatus() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(2L);
        payment.setTransactionId("pi_test_003");
        payment.setStatus("PENDING");

        given(paymentRepository.findAll()).willReturn(List.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook-fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "payment_intent.payment_failed",
                                  "data": {
                                    "object": {
                                      "id": "pi_test_003",
                                      "status": "requires_payment_method"
                                    }
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        assertThat(payment.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void webhookIgnoresUnknownTransactionId() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(3L);
        payment.setTransactionId("pi_different");
        payment.setStatus("PENDING");

        given(paymentRepository.findAll()).willReturn(List.of(payment));

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-webhook-noop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "payment_intent.succeeded",
                                  "data": {
                                    "object": {
                                      "id": "pi_not_found",
                                      "status": "succeeded"
                                    }
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        // Status must remain unchanged
        assertThat(payment.getStatus()).isEqualTo("PENDING");
    }
}
