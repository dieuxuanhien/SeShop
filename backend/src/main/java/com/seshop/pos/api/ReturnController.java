package com.seshop.pos.api;

import com.seshop.pos.api.dto.ProcessReturnRequest;
import com.seshop.pos.api.dto.ReturnDto;
import com.seshop.pos.application.ReturnService;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.LocationAccessService;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pos/returns")
public class ReturnController {

    private static final String REFUND_PROCESS = "refund.process";

    private final ReturnService returnService;
    private final PermissionValidator permissionValidator;
    private final LocationAccessService locationAccessService;

    public ReturnController(
            ReturnService returnService,
            PermissionValidator permissionValidator,
            LocationAccessService locationAccessService) {
        this.returnService = returnService;
        this.permissionValidator = permissionValidator;
        this.locationAccessService = locationAccessService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> processReturn(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProcessReturnRequest request) {
        permissionValidator.require(REFUND_PROCESS);
        ReturnDto returnDto = returnService.processReturn(request, user.userId(), locationAccessService.scopeFor(user));

        Map<String, Object> response = new HashMap<>();
        response.put("data", returnDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
