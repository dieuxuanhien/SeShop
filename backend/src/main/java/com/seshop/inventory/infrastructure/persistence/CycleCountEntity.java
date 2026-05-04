package com.seshop.inventory.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cycle_counts")
public class CycleCountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private LocationEntity location;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "started_by", nullable = false)
    private Long startedBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @OneToMany(mappedBy = "cycleCount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CycleCountItemEntity> items = new ArrayList<>();

    @PrePersist
    protected void onStart() {
        startedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocationEntity getLocation() { return location; }
    public void setLocation(LocationEntity location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getStartedBy() { return startedBy; }
    public void setStartedBy(Long startedBy) { this.startedBy = startedBy; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }

    public List<CycleCountItemEntity> getItems() { return items; }
    public void setItems(List<CycleCountItemEntity> items) { this.items = items; }
}
