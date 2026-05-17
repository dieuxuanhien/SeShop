package com.seshop.payment.api;

import com.seshop.payment.infrastructure.StripeClient;
import com.seshop.shared.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentFeeController {

    private final StripeClient stripeClient;

    public PaymentFeeController(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    /**
     * POST /api/v1/payment/estimate-fee
     * Body: { "amount": 500000 }
     * Returns: { "fee": 17000 }
     */
    @PostMapping("/estimate-fee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estimateFee(
            @RequestBody Map<String, Object> body) {
        Object amountObj = body.get("amount");
        BigDecimal amount = BigDecimal.ZERO;
        if (amountObj instanceof Number n) {
            amount = BigDecimal.valueOf(n.doubleValue());
        } else if (amountObj instanceof String s) {
            try {
                amount = new BigDecimal(s);
            } catch (Exception ignored) {}
        }
        BigDecimal fee = stripeClient.estimateStripeFee(amount);
        return ResponseEntity.ok(ApiResponse.success(Map.of("fee", fee)));
    }
}
