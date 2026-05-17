package com.seshop.payment.infrastructure;

import com.seshop.shared.exception.BusinessException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StripeClient {

    private final StripeProperties properties;

    public StripeClient(StripeProperties properties) {
        this.properties = properties;
        if (StringUtils.hasText(properties.getSecretKey())) {
            Stripe.apiKey = properties.getSecretKey();
        }
    }

    public StripePaymentResult createPaymentIntent(BigDecimal amount, String idempotencyKey, String orderNumber) {
        if (!properties.isEnabled()) {
            throw new BusinessException("PAY_002", "Stripe integration is disabled");
        }
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new BusinessException("PAY_002", "Stripe secret key is not configured");
        }

        String currency = properties.getCurrency().toLowerCase();
        long amountInMinorUnits;
        if (isZeroDecimal(currency)) {
            amountInMinorUnits = amount.longValue();
        } else {
            amountInMinorUnits = amount.multiply(BigDecimal.valueOf(100L)).longValue();
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInMinorUnits)
                    .setCurrency(currency)
                    .putMetadata("order_number", orderNumber)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey(idempotencyKey)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params, options);

            return new StripePaymentResult(
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    paymentIntent.getClientSecret()
            );
        } catch (StripeException e) {
            throw new BusinessException("PAY_001", "Stripe API error: " + e.getMessage());
        }
    }

    private boolean isZeroDecimal(String currency) {
        return java.util.Set.of("bif", "clp", "djf", "gnf", "jpy", "kmf", "krw", "mga", "pyg", "rwf", "ugx", "vnd", "vuv", "xaf", "xof", "xpf")
                .contains(currency);
    }

    /**
     * Estimates the Stripe processing fee for a given amount based on Stripe's standard pricing formula.
     * Formula: 2.9% + fixed fee (e.g., 2500 VND or 0.30 USD).
     */
    public BigDecimal estimateStripeFee(BigDecimal amount) {
        if (!properties.isEnabled() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentageFee = amount.multiply(BigDecimal.valueOf(0.029));
        BigDecimal fixedFee = isZeroDecimal(properties.getCurrency().toLowerCase())
                ? BigDecimal.valueOf(2500) : BigDecimal.valueOf(0.30);
        return percentageFee.add(fixedFee).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public record StripePaymentResult(String transactionId, String status, String clientSecret) {
    }
}
