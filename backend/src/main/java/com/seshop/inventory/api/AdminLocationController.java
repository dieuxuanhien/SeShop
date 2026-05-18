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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/locations")
public class AdminLocationController {

    private static final String STAFF_LOCATION_ASSIGN = "staff.location.assign";
    private static final String LOCATION_SCOPE_ALL = "location.scope.all";
    private static final String INVENTORY_ADJUST = "inventory.adjust";
    private static final String INVENTORY_TRANSFER = "inventory.transfer";
    private static final String REPORT_READ = "report.read";

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
                LOCATION_SCOPE_ALL,
                INVENTORY_ADJUST,
                INVENTORY_TRANSFER,
                REPORT_READ);
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
                location.getStatus());
    }
}
