package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.commerce.api.InvoiceController;
import com.seshop.commerce.application.InvoiceService;
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
import java.util.List;
import java.util.Map;
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
 * UC27: Tax invoice and adjustment note endpoints must enforce invoice permissions.
 */
@WebMvcTest(controllers = InvoiceController.class)
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
class InvoiceControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createTaxInvoiceRequiresInvoiceManagePermission() throws Exception {
        authenticate("invoice-token", List.of("invoice.manage"));
        given(invoiceService.createTaxInvoice(any())).willReturn(Map.of(
                "invoiceId", 100L,
                "invoiceNumber", "INV-2026-05-15-100",
                "orderId", 900L,
                "status", "ISSUED"
        ));

        mockMvc.perform(post("/api/v1/invoices/tax")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("invoice-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": 900
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.invoiceId").value(100))
                .andExpect(jsonPath("$.data.status").value("ISSUED"));
    }

    @Test
    void createTaxInvoiceRejectsAuthenticatedUserWithoutInvoiceManagePermission() throws Exception {
        authenticate("invoice-viewer-token", List.of("order.read"));

        mockMvc.perform(post("/api/v1/invoices/tax")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("invoice-viewer-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invoice-forbidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": 900
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));

        then(invoiceService).shouldHaveNoInteractions();
    }

    @Test
    void createAdjustmentRequiresInvoiceManagePermission() throws Exception {
        authenticate("invoice-adjust-token", List.of("invoice.manage"));
        given(invoiceService.createAdjustment(eq(100L), any())).willReturn(Map.of(
                "invoiceId", 100L,
                "invoiceNumber", "INV-2026-05-15-100",
                "adjustmentAmount", new BigDecimal("50000"),
                "reason", "Tax correction",
                "status", "ADJUSTED"
        ));

        mockMvc.perform(post("/api/v1/invoices/100/adjustments")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("invoice-adjust-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invoice-adjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "Tax correction",
                                  "deltaAmount": 50000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("ADJUSTED"));
    }

    private void authenticate(String token, List<String> permissions) {
        AuthenticatedUser principal = new AuthenticatedUser(42L, "finance.user", "STAFF", permissions);
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
}
