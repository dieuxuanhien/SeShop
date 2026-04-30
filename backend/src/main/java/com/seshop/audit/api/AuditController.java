package com.seshop.audit.api;

import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.security.PermissionValidator;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
public class AuditController {

    private final PermissionValidator permissionValidator;

    public AuditController(PermissionValidator permissionValidator) {
        this.permissionValidator = permissionValidator;
    }

    @GetMapping
    public ApiResponse<List<String>> listAuditLogs() {
        permissionValidator.require("audit.read");
        return ApiResponse.success(List.of(), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
