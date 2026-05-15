package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.refund.api.RefundController;
import com.seshop.refund.api.dto.RefundDto;
import com.seshop.refund.api.dto.ReturnDto;
import com.seshop.refund.application.RefundService;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.PermissionValidator;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
 * UC9: Process refund – WebMvc contract for /api/v1/returns and /api/v1/refunds.
 */
@WebMvcTest(controllers = RefundController.class)
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
class RefundControllerContractTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefundService refundService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpJwt() {
        List<String> permissions = List.of("refund.process");
        AuthenticatedUser principal = new AuthenticatedUser(10L, "staff.manager", "STAFF", permissions);
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, STAFF_TOKEN, authorities);
        given(jwtTokenProvider.validate(STAFF_TOKEN)).willReturn(true);
        given(jwtTokenProvider.authentication(STAFF_TOKEN)).willReturn(auth);
    }

    @Test
    void createReturnReturnsCreatedDto() throws Exception {
        ReturnDto dto = new ReturnDto();
        dto.setReturnId(201L);
        dto.setOrderId(1001L);
        dto.setReason("Size too small");
        dto.setStatus("PENDING");
        dto.setCreatedAt(OffsetDateTime.now());
        given(refundService.createReturn(any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/returns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": 1001,
                                  "reason": "Size too small",
                                  "items": [
                                    {"orderItemId": 5001, "qty": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.returnId").value(201))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void createReturnRejectsAuthenticatedUserWithoutRefundProcessPermission() throws Exception {
        String token = "refund-viewer-token";
        List<String> permissions = List.of("order.read");
        AuthenticatedUser principal = new AuthenticatedUser(11L, "staff.viewer", "STAFF", permissions);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(auth);

        mockMvc.perform(post("/api/v1/returns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-refund-forbidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": 1001,
                                  "reason": "Size too small",
                                  "items": [
                                    {"orderItemId": 5001, "qty": 1}
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
    }

    @Test
    void createReturnReturnsBadRequestWhenFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/returns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invalid-return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_001"));
    }

    @Test
    void approveReturnReturnsApprovedDto() throws Exception {
        ReturnDto dto = new ReturnDto();
        dto.setReturnId(201L);
        dto.setStatus("APPROVED");
        given(refundService.approveReturn(201L)).willReturn(dto);

        mockMvc.perform(post("/api/v1/returns/201/approve")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-approve-return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void createRefundReturnsCreatedDto() throws Exception {
        RefundDto dto = new RefundDto();
        dto.setRefundId(301L);
        dto.setOrderId(1001L);
        dto.setAmount(new BigDecimal("548000"));
        dto.setStatus("COMPLETED");
        given(refundService.createRefund(any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/refunds")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": 1001,
                                  "paymentId": 501,
                                  "returnRequestId": 201,
                                  "amount": 548000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.refundId").value(301))
                .andExpect(jsonPath("$.data.amount").value(548000));
    }

    @Test
    void getRefundByIdReturnsDto() throws Exception {
        RefundDto dto = new RefundDto();
        dto.setRefundId(301L);
        dto.setStatus("COMPLETED");
        given(refundService.getRefund(301L)).willReturn(dto);

        mockMvc.perform(get("/api/v1/refunds/301")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-get-refund"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.refundId").value(301))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
