package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.commerce.api.CheckoutController;
import com.seshop.commerce.api.dto.CheckoutResponse;
import com.seshop.commerce.application.OrderService;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
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
 * UC15: Checkout and pay – WebMvc contract for POST /api/v1/checkout.
 */
@WebMvcTest(controllers = CheckoutController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = "seshop.cors.allowed-origins=http://localhost:5173")
class CheckoutControllerContractTest {

    private static final String CUSTOMER_TOKEN = "customer-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpJwt() {
        List<String> permissions = List.of();
        AuthenticatedUser principal = new AuthenticatedUser(42L, "demo.customer", "CUSTOMER", permissions);
        List<SimpleGrantedAuthority> authorities = List.of();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, CUSTOMER_TOKEN, authorities);
        given(jwtTokenProvider.validate(CUSTOMER_TOKEN)).willReturn(true);
        given(jwtTokenProvider.authentication(CUSTOMER_TOKEN)).willReturn(auth);
    }

    @Test
    void checkoutCodReturnsCreatedOrderNumber() throws Exception {
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(1001L);
        response.setOrderNumber("ORD-ABC12345");
        response.setPaymentStatus("PENDING");
        response.setShipmentStatus("PENDING");
        given(orderService.checkout(anyLong(), any())).willReturn(response);

        mockMvc.perform(post("/api/v1/checkout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + CUSTOMER_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": 1,
                                  "shippingAddress": {
                                    "fullName": "Nguyen Van A",
                                    "phoneNumber": "+84901000001",
                                    "line1": "123 Le Loi",
                                    "ward": "Ben Nghe",
                                    "district": "District 1",
                                    "city": "Ho Chi Minh"
                                  },
                                  "paymentProvider": "COD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderId").value(1001))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-ABC12345"))
                .andExpect(jsonPath("$.data.paymentStatus").value("PENDING"));
    }

    @Test
    void checkoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/checkout")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-no-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cartId":1,"shippingAddress":{},"paymentProvider":"COD"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void checkoutReturnsBadRequestWhenCartIdMissing() throws Exception {
        mockMvc.perform(post("/api/v1/checkout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + CUSTOMER_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invalid-checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shippingAddress": {
                                    "fullName": "Test"
                                  },
                                  "paymentProvider": "COD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_001"));
    }
}
