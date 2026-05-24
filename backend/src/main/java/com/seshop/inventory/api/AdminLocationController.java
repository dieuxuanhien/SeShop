package com.seshop.inventory.api;

import com.seshop.inventory.api.dto.LocationDto;
import com.seshop.inventory.infrastructure.persistence.LocationEntity;
import com.seshop.inventory.infrastructure.persistence.LocationRepository;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.PermissionValidator;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/admin/locations")
public class AdminLocationController {

    private static final String STAFF_LOCATION_ASSIGN = "staff.location.assign";
    private static final String LOCATION_SCOPE_ALL = "location.scope.all";

    private final LocationRepository locationRepository;
    private final PermissionValidator permissionValidator;

    public AdminLocationController(LocationRepository locationRepository, PermissionValidator permissionValidator) {
        this.locationRepository = locationRepository;
        this.permissionValidator = permissionValidator;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listLocations() {
        permissionValidator.requireAny(
                STAFF_LOCATION_ASSIGN,
                LOCATION_SCOPE_ALL);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "items",
                locationRepository.findAll(Sort.by("displayName")).stream()
                        .map(this::toDto)
                        .toList())));
    }

    private LocationDto toDto(LocationEntity location) {
        return new LocationDto(
                location.getId(),
                location.getCode(),
                location.getDisplayName(),
                location.getLocationType(),
                location.getStatus(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAddressText());
    }

    public record LocationMutationRequest(
            @NotBlank String code,
            @NotBlank String displayName,
            @NotBlank String locationType,
            @NotBlank String status,
            Double latitude,
            Double longitude,
            String addressText
    ) {}

    @PostMapping
    public ResponseEntity<ApiResponse<LocationDto>> createLocation(@Valid @RequestBody LocationMutationRequest request) {
        permissionValidator.requireAny(LOCATION_SCOPE_ALL);
        LocationEntity entity = new LocationEntity();
        entity.setCode(request.code());
        entity.setDisplayName(request.displayName());
        entity.setLocationType(request.locationType());
        entity.setStatus(request.status());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setAddressText(request.addressText());
        LocationEntity saved = locationRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.success(toDto(saved)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LocationDto>> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationMutationRequest request) {
        permissionValidator.requireAny(LOCATION_SCOPE_ALL);
        LocationEntity entity = locationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        entity.setCode(request.code());
        entity.setDisplayName(request.displayName());
        entity.setLocationType(request.locationType());
        entity.setStatus(request.status());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setAddressText(request.addressText());
        LocationEntity saved = locationRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.success(toDto(saved)));
    }
}
