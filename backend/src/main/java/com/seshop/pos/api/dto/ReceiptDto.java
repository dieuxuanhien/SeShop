package com.seshop.pos.api.dto;

import java.time.OffsetDateTime;

public class ReceiptDto {

    private Long id;
    private Long transactionId;
    private String receiptNumber;
    private String receiptContent;
    private OffsetDateTime issuedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getReceiptContent() { return receiptContent; }
    public void setReceiptContent(String receiptContent) { this.receiptContent = receiptContent; }

    public OffsetDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(OffsetDateTime issuedAt) { this.issuedAt = issuedAt; }
}
