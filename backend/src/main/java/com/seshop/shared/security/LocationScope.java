package com.seshop.shared.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public record LocationScope(boolean allLocations, Set<Long> locationIds) {

    public static LocationScope all() {
        return new LocationScope(true, Set.of());
    }

    public static LocationScope restricted(Collection<Long> locationIds) {
        Set<Long> normalized = locationIds == null
                ? Set.of()
                : locationIds.stream()
                        .filter(id -> id != null)
                        .collect(Collectors.toUnmodifiableSet());
        return new LocationScope(false, normalized);
    }

    public boolean allows(Long locationId) {
        return allLocations || (locationId != null && locationIds.contains(locationId));
    }

    public boolean isEmpty() {
        return !allLocations && locationIds.isEmpty();
    }
}
