package com.seshop.identity.api;

import java.time.OffsetDateTime;

public record StaffLocationAssignmentDto(
        Long assignmentId,
        Long locationId,
        String locationName,
        OffsetDateTime assignedAt
) {
}
