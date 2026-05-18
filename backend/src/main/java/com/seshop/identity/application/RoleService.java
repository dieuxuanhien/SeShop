package com.seshop.identity.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.identity.api.AdminRoleDto;
import com.seshop.identity.api.AdminUserDto;
import com.seshop.identity.api.StaffLocationAssignmentDto;
import com.seshop.identity.api.CreateAdminUserRequest;
import com.seshop.identity.api.CreateRoleRequest;
import com.seshop.identity.api.PermissionDto;
import com.seshop.identity.api.UpdateAdminUserRequest;
import com.seshop.identity.api.UpdateRoleRequest;
import com.seshop.identity.api.UserRoleAssignmentDto;
import com.seshop.identity.domain.RoleStatus;
import com.seshop.identity.domain.UserStatus;
import com.seshop.identity.domain.UserType;
import com.seshop.identity.infrastructure.persistence.PermissionEntity;
import com.seshop.identity.infrastructure.persistence.PermissionRepository;
import com.seshop.identity.infrastructure.persistence.RoleEntity;
import com.seshop.identity.infrastructure.persistence.RolePermissionEntity;
import com.seshop.identity.infrastructure.persistence.RolePermissionRepository;
import com.seshop.identity.infrastructure.persistence.RoleRepository;
import com.seshop.identity.infrastructure.persistence.StaffLocationAssignmentEntity;
import com.seshop.identity.infrastructure.persistence.StaffLocationAssignmentRepository;
import com.seshop.identity.infrastructure.persistence.UserEntity;
import com.seshop.identity.infrastructure.persistence.UserRepository;
import com.seshop.identity.infrastructure.persistence.UserRoleEntity;
import com.seshop.identity.infrastructure.persistence.UserRoleRepository;
import com.seshop.inventory.infrastructure.persistence.LocationRepository;
import com.seshop.shared.exception.DuplicateResourceException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final StaffLocationAssignmentRepository staffLocationAssignmentRepository;
    private final LocationRepository locationRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
	                       RolePermissionRepository rolePermissionRepository,
	                       UserRepository userRepository,
	                       UserRoleRepository userRoleRepository,
                           StaffLocationAssignmentRepository staffLocationAssignmentRepository,
                           LocationRepository locationRepository,
	                       AuditService auditService,
	                       PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.staffLocationAssignmentRepository = staffLocationAssignmentRepository;
        this.locationRepository = locationRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RoleEntity createRole(CreateRoleRequest request) {
        ensureRoleNameAvailable(request.name().trim(), null);
        RoleEntity role = new RoleEntity();
        role.setName(request.name().trim());
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

    @Transactional(readOnly = true)
    public List<AdminRoleDto> listRoleDetails() {
        return roleRepository.findAll(Sort.by("name")).stream()
                .map(this::toRoleDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll(Sort.by("code")).stream()
                .map(permission -> new PermissionDto(permission.getId(), permission.getCode(), permission.getDescription()))
                .toList();
    }

    @Transactional
    public RoleEntity updateRole(Long roleId, UpdateRoleRequest request) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        ensureRoleNameAvailable(request.name().trim(), roleId);

        String previousName = role.getName();
        RoleStatus previousStatus = role.getStatus();
        role.setName(request.name().trim());
        role.setDescription(request.description());
        role.setStatus(request.status());
        RoleEntity saved = roleRepository.save(role);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("previousName", previousName);
        metadata.put("roleName", saved.getName());
        metadata.put("previousStatus", previousStatus.name());
        metadata.put("status", saved.getStatus().name());
        metadata.put("description", saved.getDescription());
        auditService.write(AuditAction.ROLE_UPDATED, "Role", saved.getId().toString(), metadata);
        return saved;
    }

    @Transactional
    public void deleteRole(Long roleId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        List<UserRoleEntity> activeAssignments = userRoleRepository.findByRoleIdAndRevokedAtIsNull(roleId);
        OffsetDateTime revokedAt = OffsetDateTime.now();
        activeAssignments.forEach(assignment -> assignment.setRevokedAt(revokedAt));
        userRoleRepository.saveAll(activeAssignments);
        role.setStatus(RoleStatus.INACTIVE);
        roleRepository.save(role);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("roleName", role.getName());
        metadata.put("revokedAssignments", activeAssignments.size());
        auditService.write(AuditAction.ROLE_DELETED, "Role", roleId.toString(), metadata);
    }

    @Transactional
    public void assignPermissionsToRole(Long roleId, List<String> permissionCodes) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        List<String> previousPermissionCodes = rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .sorted()
                .toList();

        List<String> distinctCodes = permissionCodes.stream()
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
        List<PermissionEntity> permissions = permissionRepository.findByCodeIn(distinctCodes);
        if (permissions.size() != distinctCodes.size()) {
            throw new IllegalArgumentException("Some permissions not found");
        }

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

    @Transactional
    public void assignLocationToUser(Long userId, Long locationId, Long assignedByUserId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User must be active before location assignment");
        }
        if (user.getUserType() == UserType.CUSTOMER) {
            throw new IllegalArgumentException("Only staff or admin users can be assigned to locations");
        }
        locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        if (staffLocationAssignmentRepository
                .findByUserIdAndLocationIdAndRevokedAtIsNull(userId, locationId)
                .isPresent()) {
            throw new IllegalArgumentException("Location already assigned to user");
        }

        UserEntity assignedBy = assignedByUserId != null ? userRepository.findById(assignedByUserId).orElse(null) : null;
        StaffLocationAssignmentEntity assignment = new StaffLocationAssignmentEntity();
        assignment.setUser(user);
        assignment.setLocationId(locationId);
        assignment.setAssignedBy(assignedBy);
        staffLocationAssignmentRepository.save(assignment);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("locationId", locationId);
        metadata.put("assignedByUserId", assignedByUserId);
        auditService.write(AuditAction.USER_LOCATION_ASSIGNED, "User", userId.toString(), metadata);
    }

    @Transactional
    public void revokeLocationFromUser(Long userId, Long assignmentId) {
        StaffLocationAssignmentEntity assignment = staffLocationAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Staff location assignment not found"));
        if (!assignment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Staff location assignment does not belong to user");
        }

        assignment.setRevokedAt(OffsetDateTime.now());
        staffLocationAssignmentRepository.save(assignment);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("assignmentId", assignmentId);
        metadata.put("locationId", assignment.getLocationId());
        auditService.write(AuditAction.USER_LOCATION_REVOKED, "User", userId.toString(), metadata);
    }

    @Transactional(readOnly = true)
    public List<AdminUserDto> listUsers() {
        return userRepository.findAll(Sort.by("username")).stream()
                .map(this::toUserDto)
                .toList();
    }

    @Transactional
    public AdminUserDto createUser(CreateAdminUserRequest request) {
        ensureUserUnique(request.username().trim(), request.email().trim().toLowerCase(), request.phoneNumber().trim(), null);

        UserEntity user = new UserEntity();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setUserType(request.userType());
        user.setStatus(request.status());
        UserEntity saved = userRepository.save(user);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("username", saved.getUsername());
        metadata.put("userType", saved.getUserType().name());
        metadata.put("status", saved.getStatus().name());
        auditService.write(AuditAction.USER_CREATED, "User", saved.getId().toString(), metadata);
        return toUserDto(saved);
    }

    @Transactional
    public AdminUserDto updateUser(Long userId, UpdateAdminUserRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ensureUserUnique(
                request.username().trim(),
                request.email().trim().toLowerCase(),
                request.phoneNumber().trim(),
                userId
        );

        String previousStatus = user.getStatus().name();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhoneNumber(request.phoneNumber().trim());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setUserType(request.userType());
        user.setStatus(request.status());
        UserEntity saved = userRepository.save(user);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("username", saved.getUsername());
        metadata.put("userType", saved.getUserType().name());
        metadata.put("previousStatus", previousStatus);
        metadata.put("status", saved.getStatus().name());
        auditService.write(AuditAction.USER_UPDATED, "User", saved.getId().toString(), metadata);
        return toUserDto(saved);
    }

    @Transactional
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<UserRoleEntity> activeAssignments = userRoleRepository.findByUserIdAndRevokedAtIsNull(userId);
        OffsetDateTime revokedAt = OffsetDateTime.now();
        activeAssignments.forEach(assignment -> assignment.setRevokedAt(revokedAt));
        userRoleRepository.saveAll(activeAssignments);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("username", user.getUsername());
        metadata.put("revokedAssignments", activeAssignments.size());
        auditService.write(AuditAction.USER_DELETED, "User", userId.toString(), metadata);
    }

    public AdminRoleDto toRoleDto(RoleEntity role) {
        List<String> permissionCodes = rolePermissionRepository.findByRoleId(role.getId()).stream()
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .sorted()
                .toList();
        return new AdminRoleDto(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getStatus().name(),
                permissionCodes
        );
    }

    private AdminUserDto toUserDto(UserEntity user) {
        List<UserRoleEntity> activeRoles = userRoleRepository.findByUserIdAndRevokedAtIsNull(user.getId()).stream()
                .filter(userRole -> userRole.getRole().getStatus() == RoleStatus.ACTIVE)
                .toList();
        List<UserRoleAssignmentDto> roleAssignments = activeRoles.stream()
                .map(userRole -> new UserRoleAssignmentDto(
                        userRole.getId(),
                        userRole.getRole().getId(),
                        userRole.getRole().getName(),
                        userRole.getAssignedAt()
                ))
                .toList();
        List<String> permissions = activeRoles.stream()
                .flatMap(userRole -> rolePermissionRepository.findByRoleId(userRole.getRole().getId()).stream())
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .distinct()
                .sorted()
                .toList();
        List<StaffLocationAssignmentDto> assignedLocations = staffLocationAssignmentRepository
                .findByUserIdAndRevokedAtIsNull(user.getId()).stream()
                .map(assignment -> new StaffLocationAssignmentDto(
                        assignment.getId(),
                        assignment.getLocationId(),
                        locationName(assignment.getLocationId()),
                        assignment.getAssignedAt()
                ))
                .toList();
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserType().name(),
                user.getStatus().name(),
                roleAssignments,
                assignedLocations,
                permissions
        );
    }

    private String locationName(Long locationId) {
        return locationRepository.findById(locationId)
                .map(location -> location.getDisplayName())
                .orElse("Location " + locationId);
    }

    private void ensureRoleNameAvailable(String name, Long existingRoleId) {
        roleRepository.findByName(name)
                .filter(role -> existingRoleId == null || !role.getId().equals(existingRoleId))
                .ifPresent(role -> {
                    throw new DuplicateResourceException("ROLE_409", "Role already exists");
                });
    }

    private void ensureUserUnique(String username, String email, String phoneNumber, Long existingUserId) {
        userRepository.findByUsername(username)
                .filter(user -> existingUserId == null || !user.getId().equals(existingUserId))
                .ifPresent(user -> {
                    throw new DuplicateResourceException("USER_409", "Username already exists");
                });
        userRepository.findByEmail(email)
                .filter(user -> existingUserId == null || !user.getId().equals(existingUserId))
                .ifPresent(user -> {
                    throw new DuplicateResourceException("USER_409", "Email already exists");
                });
        userRepository.findByPhoneNumber(phoneNumber)
                .filter(user -> existingUserId == null || !user.getId().equals(existingUserId))
                .ifPresent(user -> {
                    throw new DuplicateResourceException("USER_409", "Phone number already exists");
                });
    }
}
