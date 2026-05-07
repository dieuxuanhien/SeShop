package com.seshop.audit.api;

import com.seshop.audit.infrastructure.persistence.AuditLogEntity;
import com.seshop.audit.infrastructure.persistence.AuditLogRepository;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.security.PermissionValidator;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditController {

    private final PermissionValidator permissionValidator;
    private final AuditLogRepository auditLogRepository;

    public AuditController(PermissionValidator permissionValidator, AuditLogRepository auditLogRepository) {
        this.permissionValidator = permissionValidator;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ApiResponse<List<AuditLogDto>> listAuditLogs() {
        permissionValidator.require("audit.read");
        List<AuditLogDto> logs = auditLogRepository.findAll().stream()
                .map(this::mapLog)
                .toList();
        return ApiResponse.success(logs, MDC.get(TraceIdFilter.TRACE_ID));
    }

    private AuditLogDto mapLog(AuditLogEntity entity) {
        return new AuditLogDto(
                entity.getId(),
                entity.getAction(),
                entity.getActorUserId() == null ? "System" : "User " + entity.getActorUserId(),
                entity.getTargetType() + (entity.getTargetId() == null ? "" : ": " + entity.getTargetId()),
                "OK",
                entity.getCreatedAt());
    }

    public record AuditLogDto(
            Long id,
            String action,
            String actor,
            String target,
            String status,
            OffsetDateTime createdAt) {
    }
}
