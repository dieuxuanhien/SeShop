package com.seshop.identity.api;

import com.seshop.identity.application.RoleService;
import com.seshop.identity.infrastructure.persistence.RoleEntity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleEntity role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", Map.of(
                        "id", role.getId(),
                        "name", role.getName(),
                        "status", role.getStatus().name()
                ),
                "meta", Map.of("traceId", UUID.randomUUID().toString())
        ));
    }

    @GetMapping("/roles")
    public ResponseEntity<?> listRoles() {
        List<RoleEntity> roles = roleService.listRoles();
        // In a real application, this would be paginated
        return ResponseEntity.ok(Map.of(
                "data", Map.of("items", roles),
                "meta", Map.of("traceId", UUID.randomUUID().toString())
        ));
    }

    @PostMapping("/roles/{roleId}/permissions")
    public ResponseEntity<?> assignPermissions(@PathVariable Long roleId,
                                               @Valid @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissionsToRole(roleId, request.permissionCodes());
        return ResponseEntity.ok(Map.of(
                "data", Map.of("success", true),
                "meta", Map.of("traceId", UUID.randomUUID().toString())
        ));
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId,
                                              @Valid @RequestBody AssignRoleRequest request) {
        // Hardcoded assignedByUserId = 1 for simplicity until full auth context is built
        roleService.assignRoleToUser(userId, request.roleId(), 1L);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "data", Map.of("success", true),
                "meta", Map.of("traceId", UUID.randomUUID().toString())
        ));
    }

    @DeleteMapping("/users/{userId}/roles/{assignmentId}")
    public ResponseEntity<?> revokeRoleFromUser(@PathVariable Long userId,
                                                @PathVariable Long assignmentId) {
        roleService.revokeRoleFromUser(userId, assignmentId);
        return ResponseEntity.noContent().build();
    }
}
