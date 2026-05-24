package com.seshop.inventory.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "locations")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "location_type", nullable = false, length = 20)
    private String locationType;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "address_text", length = 255)
    private String addressText;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddressText() { return addressText; }
    public void setAddressText(String addressText) { this.addressText = addressText; }
}
