package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.marketing.api.DiscountController;
import com.seshop.marketing.api.dto.DiscountDto;
import com.seshop.marketing.api.dto.DiscountValidationResponse;
import com.seshop.marketing.application.DiscountService;
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
 * UC10: Manage discount codes – WebMvc contract for /api/v1/discounts and /api/v1/staff/discounts.
 */
@WebMvcTest(controllers = DiscountController.class)
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
class DiscountControllerContractTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiscountService discountService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpJwt() {
        List<String> permissions = List.of("promo.manage");
        AuthenticatedUser principal = new AuthenticatedUser(10L, "staff.manager", "STAFF", permissions);
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, STAFF_TOKEN, authorities);
        given(jwtTokenProvider.validate(STAFF_TOKEN)).willReturn(true);
        given(jwtTokenProvider.authentication(STAFF_TOKEN)).willReturn(auth);
    }

    @Test
    void validateDiscountReturnsValidResponse() throws Exception {
        DiscountValidationResponse resp = new DiscountValidationResponse();
        resp.setValid(true);
        resp.setDiscountAmount(new BigDecimal("100000"));
        given(discountService.validateDiscount(any())).willReturn(resp);

        mockMvc.perform(post("/api/v1/discounts/validate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"VINTAGE10","orderSubtotal":1000000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.discountAmount").value(100000));
    }

    @Test
    void createDiscountReturnsCreatedWithId() throws Exception {
        DiscountDto dto = discountDto(99L, "SUMMER20", "PERCENT", "20");
        given(discountService.createDiscount(any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/staff/discounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-create-disc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"SUMMER20",
                                  "discountType":"PERCENT",
                                  "discountValue":20,
                                  "minSpend":500000,
                                  "maxUses":100,
                                  "startAt":"2026-06-01T00:00:00+07:00",
                                  "endAt":"2026-07-01T00:00:00+07:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.code").value("SUMMER20"));
    }

    @Test
    void createDiscountRejectsAuthenticatedUserWithoutPromoManagePermission() throws Exception {
        String token = "discount-viewer-token";
        List<String> permissions = List.of("order.read");
        AuthenticatedUser principal = new AuthenticatedUser(11L, "staff.viewer", "STAFF", permissions);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(auth);

        mockMvc.perform(post("/api/v1/staff/discounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-discount-forbidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"SUMMER20","discountType":"PERCENT","discountValue":20}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
    }

    @Test
    void listDiscountsReturnsArray() throws Exception {
        given(discountService.listDiscounts()).willReturn(List.of(
                discountDto(1L, "CODE1", "PERCENT", "10"),
                discountDto(2L, "CODE2", "FIXED", "50000")
        ));

        mockMvc.perform(get("/api/v1/staff/discounts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-list-disc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void updateDiscountReturnsUpdatedDto() throws Exception {
        DiscountDto updated = discountDto(1L, "CODE1", "FIXED", "30000");
        given(discountService.updateDiscount(anyLong(), any())).willReturn(updated);

        mockMvc.perform(put("/api/v1/staff/discounts/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-upd-disc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "discountType":"FIXED",
                                  "discountValue":30000,
                                  "minSpend":0,
                                  "startAt":"2026-06-01T00:00:00+07:00",
                                  "endAt":"2026-07-01T00:00:00+07:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.discountType").value("FIXED"));
    }

    @Test
    void deactivateDiscountReturns200() throws Exception {
        willDoNothing().given(discountService).deactivateDiscount(1L);

        mockMvc.perform(delete("/api/v1/staff/discounts/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-del-disc"))
                .andExpect(status().isOk());
    }

    private DiscountDto discountDto(Long id, String code, String type, String value) {
        DiscountDto dto = new DiscountDto();
        dto.setId(id);
        dto.setCode(code);
        dto.setDiscountType(type);
        dto.setDiscountValue(new BigDecimal(value));
        dto.setStatus("ACTIVE");
        dto.setStartAt(OffsetDateTime.now().minusDays(1));
        dto.setEndAt(OffsetDateTime.now().plusDays(30));
        return dto;
    }
}
