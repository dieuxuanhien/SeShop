package com.seshop.shared.domain;

import java.time.OffsetDateTime;

public interface DomainEvent {
    String name();

    OffsetDateTime occurredAt();
}
