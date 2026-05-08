package com.seshop.pos.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pos_shifts")
public class PosShiftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cashier_user_id", nullable = false)
    private Long staffId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "opened_at", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "closed_at")
    private OffsetDateTime endTime;

    @Transient
    private BigDecimal startingCash;

    @Transient
    private BigDecimal endingCash;

    @Column(nullable = false, length = 20)
    private String status;

    @PrePersist
    protected void onOpen() {
        if (startTime == null) {
            startTime = OffsetDateTime.now();
        }
    }

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
}
