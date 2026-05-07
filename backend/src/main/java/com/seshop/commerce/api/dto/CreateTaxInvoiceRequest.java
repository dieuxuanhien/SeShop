package com.seshop.commerce.api.dto;

import jakarta.validation.constraints.NotNull;

public class CreateTaxInvoiceRequest {

    @NotNull
    private Long orderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
