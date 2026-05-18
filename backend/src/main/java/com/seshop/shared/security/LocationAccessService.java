package com.seshop.shared.security;

import com.seshop.identity.infrastructure.persistence.StaffLocationAssignmentRepository;
import com.seshop.shared.exception.ForbiddenOperationException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LocationAccessService {

    public static final String LOCATION_SCOPE_ALL = "location.scope.all";

    private final StaffLocationAssignmentRepository assignmentRepository;
    private final PermissionValidator permissionValidator;

    public LocationAccessService(
            StaffLocationAssignmentRepository assignmentRepository,
            PermissionValidator permissionValidator) {
        this.assignmentRepository = assignmentRepository;
        this.permissionValidator = permissionValidator;
    }

    public LocationScope scopeFor(AuthenticatedUser user) {
        if (permissionValidator.hasPermission(LOCATION_SCOPE_ALL)) {
            return LocationScope.all();
        }
        if (user == null || user.userId() == null) {
            return LocationScope.restricted(List.of());
        }
        return LocationScope.restricted(
                assignmentRepository.findByUserIdAndRevokedAtIsNull(user.userId()).stream()
                        .map(assignment -> assignment.getLocationId())
                        .toList());
    }

    public void requireLocationAccess(AuthenticatedUser user, Long locationId) {
        LocationScope scope = scopeFor(user);
        if (!scope.allows(locationId)) {
            throw new ForbiddenOperationException("Missing location access: " + locationId);
        }
    }
}
