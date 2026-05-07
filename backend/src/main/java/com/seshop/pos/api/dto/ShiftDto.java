package com.seshop.pos.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ShiftDto {

    private Long id;
    private Long staffId;
    private Long locationId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private BigDecimal startingCash;
    private BigDecimal endingCash;
    private String status;
    private String registerName;
    private OffsetDateTime openedAt;
    private Integer transactionCount;
    private BigDecimal cardPaymentsTotal;
    private BigDecimal expectedCash;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getStartingCash() { return startingCash; }
    public void setStartingCash(BigDecimal startingCash) { this.startingCash = startingCash; }

    public BigDecimal getEndingCash() { return endingCash; }
    public void setEndingCash(BigDecimal endingCash) { this.endingCash = endingCash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegisterName() { return registerName; }
    public void setRegisterName(String registerName) { this.registerName = registerName; }

    public OffsetDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(OffsetDateTime openedAt) { this.openedAt = openedAt; }

    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }

    public BigDecimal getCardPaymentsTotal() { return cardPaymentsTotal; }
    public void setCardPaymentsTotal(BigDecimal cardPaymentsTotal) { this.cardPaymentsTotal = cardPaymentsTotal; }

    public BigDecimal getExpectedCash() { return expectedCash; }
    public void setExpectedCash(BigDecimal expectedCash) { this.expectedCash = expectedCash; }
}
