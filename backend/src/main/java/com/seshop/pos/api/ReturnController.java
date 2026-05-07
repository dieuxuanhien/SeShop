package com.seshop.pos.api;

import com.seshop.pos.api.dto.ProcessReturnRequest;
import com.seshop.pos.api.dto.ReturnDto;
import com.seshop.pos.application.ReturnService;
import com.seshop.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pos/returns")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> processReturn(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProcessReturnRequest request) {
        ReturnDto returnDto = returnService.processReturn(request, user.userId());

        Map<String, Object> response = new HashMap<>();
        response.put("data", returnDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
