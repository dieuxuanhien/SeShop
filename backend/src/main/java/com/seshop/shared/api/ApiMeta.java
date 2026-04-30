package com.seshop.shared.api;

import java.time.OffsetDateTime;

public record ApiMeta(String traceId, OffsetDateTime timestamp) {

    public static ApiMeta of(String traceId) {
        return new ApiMeta(traceId, OffsetDateTime.now());
    }
}
