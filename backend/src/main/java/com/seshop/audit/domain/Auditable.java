package com.seshop.audit.domain;

public interface Auditable {
    AuditAction auditAction();

    String targetType();

    String targetId();
}
