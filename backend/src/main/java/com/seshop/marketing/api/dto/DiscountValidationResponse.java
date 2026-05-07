package com.seshop.marketing.api.dto;

import java.math.BigDecimal;

public class DiscountValidationResponse {
    private boolean valid;
    private BigDecimal discountAmount;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}
