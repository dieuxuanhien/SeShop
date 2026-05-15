package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.identity.api.AssignPermissionsRequest;
import com.seshop.identity.api.AssignRoleRequest;
import com.seshop.identity.api.CreateRoleRequest;
import com.seshop.identity.api.RoleController;
import com.seshop.identity.application.RoleService;
import com.seshop.identity.domain.RoleStatus;
import com.seshop.identity.infrastructure.persistence.RoleEntity;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.PermissionValidator;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC1-UC3: Role and staff-role management must enforce backend permission codes.
 */
@WebMvcTest(controllers = RoleController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandler.class,
        PermissionValidator.class
})
@TestPropertySource(properties = "seshop.cors.allowed-origins=http://localhost:5173")
class RoleControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createRoleRejectsAuthenticatedUserWithoutRoleCreatePermission() throws Exception {
        authenticate("missing-role-create-token", List.of("audit.read"));

        mockMvc.perform(post("/api/v1/admin/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("missing-role-create-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-role-denied")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "INVENTORY_AUDITOR",
                                  "description": "Inventory read-only role"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));

        then(roleService).shouldHaveNoInteractions();
    }

    @Test
    void createRoleAllowsRoleCreatePermission() throws Exception {
        authenticate("role-create-token", List.of("role.create"));
        RoleEntity role = role(10L, "INVENTORY_AUDITOR", RoleStatus.INACTIVE);
        given(roleService.createRole(any(CreateRoleRequest.class))).willReturn(role);

        mockMvc.perform(post("/api/v1/admin/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("role-create-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-role-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "INVENTORY_AUDITOR",
                                  "description": "Inventory read-only role"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void listRolesAllowsAnyRoleAdministrationPermission() throws Exception {
        authenticate("role-list-token", List.of("role.permission.assign"));
        given(roleService.listRoles()).willReturn(List.of(role(11L, "STORE_MANAGER", RoleStatus.ACTIVE)));

        mockMvc.perform(get("/api/v1/admin/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("role-list-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-role-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("STORE_MANAGER"));
    }

    @Test
    void assignPermissionsRequiresRolePermissionAssignPermission() throws Exception {
        authenticate("assign-permissions-token", List.of("role.permission.assign"));

        mockMvc.perform(post("/api/v1/admin/roles/10/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("assign-permissions-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-permission-assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissionCodes": ["inventory.adjust", "order.read"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true));

        then(roleService).should().assignPermissionsToRole(eq(10L), eq(List.of("inventory.adjust", "order.read")));
    }

    @Test
    void assignRoleToUserRequiresStaffRoleAssignPermissionAndUsesActorId() throws Exception {
        authenticate("staff-role-assign-token", List.of("staff.role.assign"));

        mockMvc.perform(post("/api/v1/admin/users/77/roles")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("staff-role-assign-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-staff-role-assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.success").value(true));

        then(roleService).should().assignRoleToUser(77L, 10L, 42L);
    }

    @Test
    void revokeRoleFromUserRequiresStaffRoleAssignPermission() throws Exception {
        authenticate("staff-role-revoke-token", List.of("staff.role.assign"));

        mockMvc.perform(delete("/api/v1/admin/users/77/roles/99")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("staff-role-revoke-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-staff-role-revoke"))
                .andExpect(status().isNoContent());

        then(roleService).should().revokeRoleFromUser(77L, 99L);
    }

    private void authenticate(String token, List<String> permissions) {
        AuthenticatedUser principal = new AuthenticatedUser(42L, "admin.user", "STAFF", permissions);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );

        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(authentication);
    }

    private String bearerToken(String token) {
        return "Bearer " + token;
    }

    private RoleEntity role(Long id, String name, RoleStatus status) {
        RoleEntity role = new RoleEntity();
        role.setId(id);
        role.setName(name);
        role.setStatus(status);
        return role;
    }
}
