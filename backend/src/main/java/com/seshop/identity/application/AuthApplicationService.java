package com.seshop.identity.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.identity.domain.UserStatus;
import com.seshop.identity.domain.UserType;
import com.seshop.identity.infrastructure.persistence.RolePermissionRepository;
import com.seshop.identity.infrastructure.persistence.UserEntity;
import com.seshop.identity.infrastructure.persistence.UserRepository;
import com.seshop.identity.infrastructure.persistence.UserRoleEntity;
import com.seshop.identity.infrastructure.persistence.UserRoleRepository;
import com.seshop.shared.exception.DuplicateResourceException;
import com.seshop.shared.security.JwtTokenProvider;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public AuthApplicationService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public RegisterResult register(RegisterCommand command) {
        ensureUnique(command);

        UserEntity user = new UserEntity();
        user.setUsername(command.username().trim());
        user.setEmail(command.email().trim().toLowerCase());
        user.setPhoneNumber(command.phoneNumber().trim());
        user.setPasswordHash(passwordEncoder.encode(command.password()));
        user.setUserType(UserType.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        UserEntity saved = userRepository.save(user);
        auditService.write(AuditAction.USER_REGISTERED, "User", saved.getId().toString(), (String) null);

        return new RegisterResult(saved.getId(), saved.getStatus().name(), saved.getUserType().name());
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        UserEntity user = userRepository.findByUsernameOrEmail(command.usernameOrEmail(), command.usernameOrEmail())
                .filter(found -> found.getStatus() == UserStatus.ACTIVE)
                .filter(found -> passwordEncoder.matches(command.password(), found.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        List<UserRoleEntity> activeRoles = userRoleRepository.findByUserIdAndRevokedAtIsNull(user.getId());
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
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUsername(),
                user.getUserType().name(),
                permissions
        );

        auditService.write(AuditAction.USER_LOGGED_IN, "User", user.getId().toString(), (String) null);

        return new LoginResult(
                token,
                new LoginResult.UserSummary(user.getId(), user.getUsername(), user.getUserType().name(), roles, permissions)
        );
    }

    private void ensureUnique(RegisterCommand command) {
        if (userRepository.existsByUsername(command.username().trim())) {
            throw new DuplicateResourceException("AUTH_409", "Username already exists");
        }
        if (userRepository.existsByEmail(command.email().trim().toLowerCase())) {
            throw new DuplicateResourceException("AUTH_409", "Email already exists");
        }
        if (userRepository.existsByPhoneNumber(command.phoneNumber().trim())) {
            throw new DuplicateResourceException("AUTH_409", "Phone number already exists");
        }
    }
}
