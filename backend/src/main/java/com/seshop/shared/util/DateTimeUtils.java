package com.seshop.shared.util;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static OffsetDateTime utcNow() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
