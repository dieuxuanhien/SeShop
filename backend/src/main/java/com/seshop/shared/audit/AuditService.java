package com.seshop.shared.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void logAction(Long actorUserId, String action, String targetType, String targetId, Object metadata) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setActorUserId(actorUserId);
        auditLog.setAction(action);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);

        if (metadata != null) {
            try {
                auditLog.setMetadataJson(objectMapper.writeValueAsString(metadata));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize audit metadata", e);
            }
        }

        auditLogRepository.save(auditLog);
    }
}
