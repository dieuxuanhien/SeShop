package com.seshop.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.identity.api.CreateRoleRequest;
import com.seshop.identity.domain.RoleStatus;
import com.seshop.identity.domain.UserStatus;
import com.seshop.identity.domain.UserType;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private AuditService auditService;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(
                roleRepository,
                permissionRepository,
                rolePermissionRepository,
                userRepository,
                userRoleRepository,
                auditService
        );
    }

    @Test
    void createRoleWritesAuditEvent() {
        given(roleRepository.findByName("INVENTORY_AUDITOR")).willReturn(Optional.empty());
        given(roleRepository.save(any(RoleEntity.class))).willAnswer(invocation -> {
            RoleEntity role = invocation.getArgument(0);
            role.setId(10L);
            return role;
        });

        RoleEntity result = roleService.createRole(new CreateRoleRequest(
                "INVENTORY_AUDITOR",
                "Can inspect stock reports"
        ));

        assertThat(result.getStatus()).isEqualTo(RoleStatus.INACTIVE);
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.ROLE_CREATED),
                eq("Role"),
                eq("10"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue()).containsEntry("roleName", "INVENTORY_AUDITOR")
                .containsEntry("status", "INACTIVE")
                .containsEntry("description", "Can inspect stock reports");
    }

    @Test
    void assignPermissionsActivatesRoleAndAuditsBeforeAfterPermissions() {
        RoleEntity role = role(10L, "INVENTORY_AUDITOR", RoleStatus.INACTIVE);
        RolePermissionEntity previous = new RolePermissionEntity(role, permission(1L, "audit.read"));
        PermissionEntity inventoryAdjust = permission(2L, "inventory.adjust");
        PermissionEntity orderRead = permission(3L, "order.read");

        given(roleRepository.findById(10L)).willReturn(Optional.of(role));
        given(rolePermissionRepository.findByRoleId(10L)).willReturn(List.of(previous));
        given(permissionRepository.findByCodeIn(List.of("order.read", "inventory.adjust")))
                .willReturn(List.of(orderRead, inventoryAdjust));

        roleService.assignPermissionsToRole(10L, List.of("order.read", "inventory.adjust"));

        assertThat(role.getStatus()).isEqualTo(RoleStatus.ACTIVE);
        then(rolePermissionRepository).should().deleteByRoleId(10L);
        then(roleRepository).should().save(role);
        then(rolePermissionRepository).should(times(2)).save(any(RolePermissionEntity.class));
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.ROLE_PERMISSION_ASSIGNED),
                eq("Role"),
                eq("10"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("roleName", "INVENTORY_AUDITOR")
                .containsEntry("previousPermissionCodes", List.of("audit.read"))
                .containsEntry("assignedPermissionCodes", List.of("inventory.adjust", "order.read"))
                .containsEntry("status", "ACTIVE");
    }

    @Test
    void assignRoleToUserRejectsInactiveRole() {
        UserEntity user = user(77L, UserStatus.ACTIVE);
        RoleEntity inactiveRole = role(10L, "INVENTORY_AUDITOR", RoleStatus.INACTIVE);

        given(userRepository.findById(77L)).willReturn(Optional.of(user));
        given(roleRepository.findById(10L)).willReturn(Optional.of(inactiveRole));

        assertThatThrownBy(() -> roleService.assignRoleToUser(77L, 10L, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role must be active before assignment");
        then(auditService).shouldHaveNoInteractions();
    }

    @Test
    void assignRoleToUserRejectsInactiveUser() {
        UserEntity inactiveUser = user(77L, UserStatus.INACTIVE);
        RoleEntity activeRole = role(10L, "STORE_MANAGER", RoleStatus.ACTIVE);

        given(userRepository.findById(77L)).willReturn(Optional.of(inactiveUser));
        given(roleRepository.findById(10L)).willReturn(Optional.of(activeRole));

        assertThatThrownBy(() -> roleService.assignRoleToUser(77L, 10L, 42L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User must be active before role assignment");
        then(auditService).shouldHaveNoInteractions();
    }

    @Test
    void assignRoleToUserWritesAuditEvent() {
        UserEntity user = user(77L, UserStatus.ACTIVE);
        RoleEntity role = role(10L, "STORE_MANAGER", RoleStatus.ACTIVE);
        UserEntity assignedBy = user(42L, UserStatus.ACTIVE);

        given(userRepository.findById(77L)).willReturn(Optional.of(user));
        given(roleRepository.findById(10L)).willReturn(Optional.of(role));
        given(userRepository.findById(42L)).willReturn(Optional.of(assignedBy));
        given(userRoleRepository.findByUserIdAndRoleIdAndRevokedAtIsNull(77L, 10L))
                .willReturn(Optional.empty());

        roleService.assignRoleToUser(77L, 10L, 42L);

        ArgumentCaptor<UserRoleEntity> assignmentCaptor = ArgumentCaptor.forClass(UserRoleEntity.class);
        then(userRoleRepository).should().save(assignmentCaptor.capture());
        assertThat(assignmentCaptor.getValue().getAssignedBy()).isSameAs(assignedBy);
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.USER_ROLE_ASSIGNED),
                eq("User"),
                eq("77"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("roleId", 10L)
                .containsEntry("roleName", "STORE_MANAGER")
                .containsEntry("assignedByUserId", 42L);
    }

    @Test
    void revokeRoleFromUserWritesAuditEvent() {
        UserEntity user = user(77L, UserStatus.ACTIVE);
        RoleEntity role = role(10L, "STORE_MANAGER", RoleStatus.ACTIVE);
        UserRoleEntity assignment = new UserRoleEntity();
        assignment.setId(99L);
        assignment.setUser(user);
        assignment.setRole(role);

        given(userRoleRepository.findById(99L)).willReturn(Optional.of(assignment));

        roleService.revokeRoleFromUser(77L, 99L);

        assertThat(assignment.getRevokedAt()).isNotNull();
        then(userRoleRepository).should().save(assignment);
        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.USER_ROLE_REVOKED),
                eq("User"),
                eq("77"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("assignmentId", 99L)
                .containsEntry("roleId", 10L)
                .containsEntry("roleName", "STORE_MANAGER");
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }

    private RoleEntity role(Long id, String name, RoleStatus status) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        role.setName(name);
        role.setDescription(name + " description");
        role.setStatus(status);
        return role;
    }

    private PermissionEntity permission(Long id, String code) {
        PermissionEntity permission = new PermissionEntity();
        permission.setId(id);
        permission.setCode(code);
        return permission;
    }

    private UserEntity user(Long id, UserStatus status) {
        UserEntity user = new UserEntity();
        ReflectionTestUtils.setField(user, "id", id);
        user.setUsername("user." + id);
        user.setEmail("user." + id + "@example.com");
        user.setPhoneNumber("+8490000" + id);
        user.setPasswordHash("hashed");
        user.setUserType(UserType.STAFF);
        user.setStatus(status);
        return user;
    }
}
