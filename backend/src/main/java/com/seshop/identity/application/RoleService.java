package com.seshop.identity.application;

import com.seshop.identity.api.CreateRoleRequest;
import com.seshop.identity.domain.RoleStatus;
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
import java.util.List;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       RolePermissionRepository rolePermissionRepository,
                       UserRepository userRepository,
                       UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
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
        return roleRepository.save(role);
    }

    public List<RoleEntity> listRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public void assignPermissionsToRole(Long roleId, List<String> permissionCodes) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

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
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId, Long assignedByUserId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        
        UserEntity assignedBy = assignedByUserId != null ? userRepository.findById(assignedByUserId).orElse(null) : null;

        if (userRoleRepository.findByUserIdAndRoleIdAndRevokedAtIsNull(userId, roleId).isPresent()) {
            throw new IllegalArgumentException("Role already assigned to user");
        }

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setAssignedBy(assignedBy);
        userRoleRepository.save(userRole);
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
    }
}
