package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.identity.api.AuthController;
import com.seshop.identity.application.AuthApplicationService;
import com.seshop.identity.application.LoginCommand;
import com.seshop.identity.application.LoginResult;
import com.seshop.identity.application.RegisterCommand;
import com.seshop.identity.application.RegisterResult;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC12: Register account and Login – WebMvc contract for /api/v1/auth.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = "seshop.cors.allowed-origins=http://localhost:5173")
class AuthControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthApplicationService authApplicationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void registerReturnsCreatedWithUserIdAndType() throws Exception {
        given(authApplicationService.register(any(RegisterCommand.class)))
                .willReturn(new RegisterResult(101L, "ACTIVE", "CUSTOMER"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-reg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "newuser",
                                  "email": "newuser@example.com",
                                  "phoneNumber": "+84901000001",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(101))
                .andExpect(jsonPath("$.data.userType").value("CUSTOMER"));
    }

    @Test
    void registerReturnsBadRequestWhenFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invalid-reg")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "not-an-email",
                                  "phoneNumber": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_001"));
    }

    @Test
    void loginReturnsOkWithToken() throws Exception {
        LoginResult.UserSummary summary = new LoginResult.UserSummary(42L, "alice", "CUSTOMER", List.of("CUSTOMER"), List.of());
        given(authApplicationService.login(any(LoginCommand.class)))
                .willReturn(new LoginResult("jwt-token-xyz", summary));

        mockMvc.perform(post("/api/v1/auth/login")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "alice",
                                  "password": "P@ssw0rd!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token-xyz"))
                .andExpect(jsonPath("$.data.user.username").value("alice"));
    }

    @Test
    void loginReturns401ForInvalidCredentials() throws Exception {
        given(authApplicationService.login(any(LoginCommand.class)))
                .willThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-bad-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "alice",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
