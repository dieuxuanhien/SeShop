package com.seshop.identity.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.identity.api.UpdatePasswordRequest;
import com.seshop.identity.api.UpdateProfileRequest;
import com.seshop.identity.api.UserProfileDto;
import com.seshop.identity.domain.RoleStatus;
import com.seshop.identity.infrastructure.persistence.RolePermissionRepository;
import com.seshop.identity.infrastructure.persistence.UserEntity;
import com.seshop.identity.infrastructure.persistence.UserRepository;
import com.seshop.identity.infrastructure.persistence.UserRoleEntity;
import com.seshop.identity.infrastructure.persistence.UserRoleRepository;
import com.seshop.shared.exception.DuplicateResourceException;
import com.seshop.shared.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeApplicationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public MeApplicationService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User not found"));

        return toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User not found"));

        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("AUTH_409", "Username already exists");
        }
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("AUTH_409", "Email already exists");
        }
        if (!user.getPhoneNumber().equals(request.phoneNumber()) && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("AUTH_409", "Phone number already exists");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());

        UserEntity saved = userRepository.save(user);
        auditService.write(AuditAction.USER_UPDATED, "User", saved.getId().toString(), (String) null);

        return toDto(saved);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditService.write(AuditAction.USER_UPDATED, "User", user.getId().toString(), "Password changed");
    }

    private UserProfileDto toDto(UserEntity user) {
        List<UserRoleEntity> activeRoles = userRoleRepository.findByUserIdAndRevokedAtIsNull(user.getId()).stream()
                .filter(userRole -> userRole.getRole().getStatus() == RoleStatus.ACTIVE)
                .toList();

        List<String> roles = activeRoles.stream()
                .map(userRole -> userRole.getRole().getName())
                .distinct()
                .sorted()
                .toList();

        List<String> permissions = activeRoles.stream()
                .flatMap(userRole -> rolePermissionRepository.findByRoleId(userRole.getRole().getId()).stream())
                .map(rolePermission -> rolePermission.getPermission().getCode())
                .distinct()
                .sorted()
                .toList();

        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getUserType().name(),
                roles,
                permissions
        );
    }
}
