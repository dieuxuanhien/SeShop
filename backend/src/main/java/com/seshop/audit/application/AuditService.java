package com.seshop.audit.application;

import com.seshop.audit.domain.AuditAction;
import com.seshop.audit.infrastructure.persistence.AuditLogEntity;
import com.seshop.audit.infrastructure.persistence.AuditLogRepository;
import com.seshop.shared.security.AuthenticatedUser;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(AuditAction action, String targetType, String targetId, String metadataJson) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setActorUserId(currentActorUserId().orElse(null));
        entity.setAction(action.name());
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setMetadataJson(metadataJson);
        entity.setCreatedAt(OffsetDateTime.now());
        auditLogRepository.save(entity);
    }

    private Optional<Long> currentActorUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.ofNullable(user.userId());
    }
}
