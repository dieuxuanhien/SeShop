package com.seshop.identity.api;

import com.seshop.identity.application.RoleService;
import com.seshop.identity.infrastructure.persistence.RoleEntity;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.PermissionValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class RoleController {

    private static final String ROLE_CREATE = "role.create";
    private static final String ROLE_UPDATE = "role.update";
    private static final String ROLE_DELETE = "role.delete";
    private static final String ROLE_PERMISSION_ASSIGN = "role.permission.assign";
    private static final String STAFF_ROLE_ASSIGN = "staff.role.assign";
    private static final String STAFF_USER_READ = "staff.user.read";
    private static final String STAFF_USER_CREATE = "staff.user.create";
    private static final String STAFF_USER_UPDATE = "staff.user.update";
    private static final String STAFF_USER_DELETE = "staff.user.delete";
    private static final String STAFF_LOCATION_ASSIGN = "staff.location.assign";

    private final RoleService roleService;
    private final PermissionValidator permissionValidator;

    public RoleController(RoleService roleService, PermissionValidator permissionValidator) {
        this.roleService = roleService;
        this.permissionValidator = permissionValidator;
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<AdminRoleDto>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        permissionValidator.require(ROLE_CREATE);
        RoleEntity role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(roleService.toRoleDto(role)));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listRoles() {
        permissionValidator.requireAny(ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE, ROLE_PERMISSION_ASSIGN, STAFF_ROLE_ASSIGN);
        return ResponseEntity.ok(ApiResponse.success(Map.of("items", roleService.listRoleDetails())));
    }

    @PutMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<AdminRoleDto>> updateRole(@PathVariable Long roleId,
                                                                @Valid @RequestBody UpdateRoleRequest request) {
        permissionValidator.require(ROLE_UPDATE);
        RoleEntity role = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success(roleService.toRoleDto(role)));
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<?> deleteRole(@PathVariable Long roleId) {
        permissionValidator.require(ROLE_DELETE);
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listPermissions() {
        permissionValidator.requireAny(ROLE_CREATE, ROLE_UPDATE, ROLE_DELETE, ROLE_PERMISSION_ASSIGN, STAFF_ROLE_ASSIGN);
        return ResponseEntity.ok(ApiResponse.success(Map.of("items", roleService.listPermissions())));
    }

    @PostMapping("/roles/{roleId}/permissions")
    public ResponseEntity<?> assignPermissions(@PathVariable Long roleId,
                                               @Valid @RequestBody AssignPermissionsRequest request) {
        permissionValidator.require(ROLE_PERMISSION_ASSIGN);
        roleService.assignPermissionsToRole(roleId, request.permissionCodes());
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId,
                                              @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                              @Valid @RequestBody AssignRoleRequest request) {
        permissionValidator.require(STAFF_ROLE_ASSIGN);
        Long assignedByUserId = authenticatedUser == null ? null : authenticatedUser.userId();
        roleService.assignRoleToUser(userId, request.roleId(), assignedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of("success", true)));
    }

    @DeleteMapping("/users/{userId}/roles/{assignmentId}")
    public ResponseEntity<?> revokeRoleFromUser(@PathVariable Long userId,
                                                @PathVariable Long assignmentId) {
        permissionValidator.require(STAFF_ROLE_ASSIGN);
        roleService.revokeRoleFromUser(userId, assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/locations")
    public ResponseEntity<?> assignLocationToUser(@PathVariable Long userId,
                                                  @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                  @Valid @RequestBody AssignLocationRequest request) {
        permissionValidator.require(STAFF_LOCATION_ASSIGN);
        Long assignedByUserId = authenticatedUser == null ? null : authenticatedUser.userId();
        roleService.assignLocationToUser(userId, request.locationId(), assignedByUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(Map.of("success", true)));
    }

    @DeleteMapping("/users/{userId}/locations/{assignmentId}")
    public ResponseEntity<?> revokeLocationFromUser(@PathVariable Long userId,
                                                    @PathVariable Long assignmentId) {
        permissionValidator.require(STAFF_LOCATION_ASSIGN);
        roleService.revokeLocationFromUser(userId, assignmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listUsers() {
        permissionValidator.requireAny(
                STAFF_USER_READ,
                STAFF_USER_CREATE,
                STAFF_USER_UPDATE,
                STAFF_USER_DELETE,
                STAFF_ROLE_ASSIGN,
                STAFF_LOCATION_ASSIGN);
        return ResponseEntity.ok(ApiResponse.success(Map.of("items", roleService.listUsers())));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserDto>> createUser(@Valid @RequestBody CreateAdminUserRequest request) {
        permissionValidator.require(STAFF_USER_CREATE);
        AdminUserDto user = roleService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(user));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDto>> updateUser(@PathVariable Long userId,
                                                                @Valid @RequestBody UpdateAdminUserRequest request) {
        permissionValidator.require(STAFF_USER_UPDATE);
        AdminUserDto user = roleService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        permissionValidator.require(STAFF_USER_DELETE);
        roleService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
