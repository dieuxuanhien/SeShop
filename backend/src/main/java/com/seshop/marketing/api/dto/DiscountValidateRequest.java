package com.seshop.marketing.api.dto;

import java.math.BigDecimal;

public class DiscountValidateRequest {
    private String code;
    private BigDecimal orderSubtotal;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getOrderSubtotal() { return orderSubtotal; }
    public void setOrderSubtotal(BigDecimal orderSubtotal) { this.orderSubtotal = orderSubtotal; }
}
