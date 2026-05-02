package com.seshop.shared.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<?> listAuditLogs(Pageable pageable) {
        Page<AuditLogEntity> page = auditLogRepository.findAll(pageable);
        
        return ResponseEntity.ok(Map.of(
                "data", Map.of(
                        "items", page.getContent(),
                        "page", page.getNumber(),
                        "size", page.getSize(),
                        "totalElements", page.getTotalElements()
                ),
                "meta", Map.of("traceId", UUID.randomUUID().toString())
        ));
    }
}
