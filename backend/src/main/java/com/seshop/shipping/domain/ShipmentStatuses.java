package com.seshop.shipping.domain;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ShipmentStatuses {

    private static final Pattern TRACKING_NUMBER_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9_-]{2,119}$");
    private static final Pattern CARRIER_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9_-]{1,79}$");

    private ShipmentStatuses() {
    }

    public static Optional<String> normalizeCarrier(String carrier) {
        if (carrier == null || carrier.isBlank()) {
            return Optional.empty();
        }
        String normalized = carrier.trim().toUpperCase(Locale.ROOT);
        return CARRIER_PATTERN.matcher(normalized).matches() ? Optional.of(normalized) : Optional.empty();
    }

    public static Optional<String> normalizeTrackingNumber(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            return Optional.empty();
        }
        String normalized = trackingNumber.trim().toUpperCase(Locale.ROOT);
        return TRACKING_NUMBER_PATTERN.matcher(normalized).matches() ? Optional.of(normalized) : Optional.empty();
    }

    public static String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "PENDING";
        }
        String normalized = rawStatus.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        return switch (normalized) {
            case "READY_TO_PICK", "PICKING", "PICKED", "STORING", "SORTING", "SHIPPED" -> "SHIPPED";
            case "DELIVERING", "TRANSPORTING", "IN_TRANSIT", "OUT_FOR_DELIVERY" -> "IN_TRANSIT";
            case "DELIVERED", "COMPLETED" -> "DELIVERED";
            default -> "PENDING";
        };
    }
}
