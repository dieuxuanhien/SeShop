package com.seshop.audit.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.audit.domain.AuditAction;
import com.seshop.audit.infrastructure.persistence.AuditLogEntity;
import com.seshop.audit.infrastructure.persistence.AuditLogRepository;
import com.seshop.shared.security.AuthenticatedUser;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AuditAction action, String targetType, String targetId, String metadataJson) {
        save(action, targetType, targetId, metadataJson);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AuditAction action, String targetType, String targetId, Map<String, ?> metadata) {
        save(action, targetType, targetId, toJson(metadata));
    }

    private void save(AuditAction action, String targetType, String targetId, String metadataJson) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setActorUserId(currentActorUserId().orElse(null));
        entity.setAction(action.name());
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setMetadataJson(metadataJson);
        entity.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(entity);
    }

    private String toJson(Map<String, ?> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Audit metadata is not JSON serializable", e);
        }
    }

    private Optional<Long> currentActorUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.ofNullable(user.userId());
    }
}
