package com.seshop.identity.api;

import com.seshop.identity.application.AuthApplicationService;
import com.seshop.identity.application.LoginCommand;
import com.seshop.identity.application.LoginResult;
import com.seshop.identity.application.RegisterCommand;
import com.seshop.identity.application.RegisterResult;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.api.TraceIdFilter;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResult> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResult result = authApplicationService.register(new RegisterCommand(
                request.username(),
                request.email(),
                request.phoneNumber(),
                request.password()
        ));
        return ApiResponse.success(result, MDC.get(TraceIdFilter.TRACE_ID));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authApplicationService.login(new LoginCommand(
                request.usernameOrEmail(),
                request.password()
        ));
        return ApiResponse.success(result, MDC.get(TraceIdFilter.TRACE_ID));
    }
}
