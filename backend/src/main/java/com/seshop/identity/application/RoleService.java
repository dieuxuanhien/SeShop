package com.seshop.identity.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.identity.api.CreateRoleRequest;
import com.seshop.identity.domain.RoleStatus;
import com.seshop.identity.domain.UserStatus;
import com.seshop.identity.infrastructure.persistence.PermissionEntity;
import com.seshop.identity.infrastructure.persistence.PermissionRepository;
import com.seshop.identity.infrastructure.persistence.RoleEntity;
import com.seshop.identity.infrastructure.persistence.RolePermissionEntity;
import com.seshop.identity.infrastructure.persistence.RolePermissionRepository;
import com.seshop.identity.infrastructure.persistence.RoleRepository;
import com.seshop.identity.infrastructure.persistence.UserEntity;
import com.seshop.identity.infrastructure.persistence.UserRepository;
import com.seshop.identity.infrastructure.persistence.UserRoleEntity;
import com.seshop.identity.infrastructure.persistence.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditService auditService;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       RolePermissionRepository rolePermissionRepository,
                       UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       AuditService auditService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public RoleEntity createRole(CreateRoleRequest request) {
        if (roleRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Role already exists");
        }
        RoleEntity role = new RoleEntity();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setStatus(RoleStatus.INACTIVE); // Per API Spec
        RoleEntity saved = roleRepository.save(role);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("roleName", saved.getName());
        metadata.put("status", saved.getStatus().name());
        metadata.put("description", saved.getDescription());
        auditService.write(AuditAction.ROLE_CREATED, "Role", saved.getId().toString(), metadata);
        return saved;
    }

    public List<RoleEntity> listRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public void assignPermissionsToRole(Long roleId, List<String> permissionCodes) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        List<String> previousPermissionCodes = rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .sorted()
                .toList();

        List<PermissionEntity> permissions = permissionRepository.findByCodeIn(permissionCodes);
        if (permissions.size() != permissionCodes.size()) {
            throw new IllegalArgumentException("Some permissions not found");
        }

        // Overwrite strategy or additive? Usually additive or exact match. We'll do exact match for simplicity.
        rolePermissionRepository.deleteByRoleId(roleId);

        for (PermissionEntity permission : permissions) {
            RolePermissionEntity rolePermission = new RolePermissionEntity(role, permission);
            rolePermissionRepository.save(rolePermission);
        }
        if (!permissions.isEmpty() && role.getStatus() == RoleStatus.INACTIVE) {
            role.setStatus(RoleStatus.ACTIVE);
            roleRepository.save(role);
        }
        List<String> assignedPermissionCodes = permissions.stream()
                .map(PermissionEntity::getCode)
                .sorted()
                .toList();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("roleName", role.getName());
        metadata.put("previousPermissionCodes", previousPermissionCodes);
        metadata.put("assignedPermissionCodes", assignedPermissionCodes);
        metadata.put("status", role.getStatus().name());
        auditService.write(AuditAction.ROLE_PERMISSION_ASSIGNED, "Role", role.getId().toString(), metadata);
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId, Long assignedByUserId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User must be active before role assignment");
        }
        if (role.getStatus() != RoleStatus.ACTIVE) {
            throw new IllegalArgumentException("Role must be active before assignment");
        }
        
        UserEntity assignedBy = assignedByUserId != null ? userRepository.findById(assignedByUserId).orElse(null) : null;

        if (userRoleRepository.findByUserIdAndRoleIdAndRevokedAtIsNull(userId, roleId).isPresent()) {
            throw new IllegalArgumentException("Role already assigned to user");
        }

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedBy);
        userRoleRepository.save(userRole);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("roleId", roleId);
        metadata.put("roleName", role.getName());
        metadata.put("assignedByUserId", assignedByUserId);
        auditService.write(AuditAction.USER_ROLE_ASSIGNED, "User", userId.toString(), metadata);
    }

    @Transactional
    public void revokeRoleFromUser(Long userId, Long assignmentId) {
        UserRoleEntity userRole = userRoleRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("UserRole assignment not found"));

        if (!userRole.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("UserRole assignment does not belong to user");
        }

        userRole.setRevokedAt(OffsetDateTime.now());
        userRoleRepository.save(userRole);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("assignmentId", assignmentId);
        metadata.put("roleId", userRole.getRole().getId());
        metadata.put("roleName", userRole.getRole().getName());
        auditService.write(AuditAction.USER_ROLE_REVOKED, "User", userId.toString(), metadata);
    }
}
