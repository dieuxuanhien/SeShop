package com.seshop.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.commerce.api.StaffOrderController;
import com.seshop.commerce.api.dto.OrderDto;
import com.seshop.commerce.application.OrderService;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC19-UC20: Staff order operations must enforce order permission codes.
 */
@WebMvcTest(controllers = StaffOrderController.class)
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
class StaffOrderControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void listOrdersRequiresOrderReadPermission() throws Exception {
        authenticate("order-read-token", List.of("order.read"));
        given(orderService.listOrdersForStaff(0, 20))
                .willReturn(new PageImpl<>(List.of(order(1001L, "ORD-1001", "PAID")), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/staff/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("order-read-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-order-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].orderNumber").value("ORD-1001"));
    }

    @Test
    void listOrdersRejectsAuthenticatedUserWithoutOrderReadPermission() throws Exception {
        authenticate("missing-order-read-token", List.of("catalog.write"));

        mockMvc.perform(get("/api/v1/staff/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("missing-order-read-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-order-list-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));

        then(orderService).shouldHaveNoInteractions();
    }

    @Test
    void shipOrderRequiresOrderShipPermission() throws Exception {
        authenticate("order-ship-token", List.of("order.ship"));
        OrderDto shipped = order(1001L, "ORD-1001", "SHIPPED");
        given(orderService.shipOrder(eq(1001L), eq("GHN"), eq("Nguyen Van A"), eq("+84901000001"), eq("GHN-123")))
                .willReturn(shipped);

        mockMvc.perform(post("/api/v1/staff/orders/1001/ship")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("order-ship-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-order-ship")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrier": "GHN",
                                  "recipientName": "Nguyen Van A",
                                  "recipientPhone": "+84901000001",
                                  "trackingNumber": "GHN-123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SHIPPED"));
    }

    @Test
    void shipOrderRejectsOrderReadWithoutOrderShipPermission() throws Exception {
        authenticate("order-read-only-token", List.of("order.read"));

        mockMvc.perform(post("/api/v1/staff/orders/1001/ship")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken("order-read-only-token"))
                        .header(TraceIdFilter.TRACE_HEADER, "trace-order-ship-denied")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "carrier": "GHN",
                                  "recipientName": "Nguyen Van A",
                                  "recipientPhone": "+84901000001",
                                  "trackingNumber": "GHN-123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
    }

    private void authenticate(String token, List<String> permissions) {
        AuthenticatedUser principal = new AuthenticatedUser(42L, "staff.user", "STAFF", permissions);
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

    private OrderDto order(Long id, String orderNumber, String status) {
        OrderDto order = new OrderDto();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("590000"));
        return order;
    }
}
