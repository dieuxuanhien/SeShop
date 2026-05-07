package com.seshop.api;

import static org.hamcrest.Matchers.hasItems;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.inventory.api.StaffInventoryController;
import com.seshop.inventory.api.dto.CreateTransferRequest;
import com.seshop.inventory.api.dto.InventoryBalanceDto;
import com.seshop.inventory.application.InventoryService;
import com.seshop.pos.api.ReceiptController;
import com.seshop.pos.api.ShiftController;
import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.application.ReceiptService;
import com.seshop.pos.application.ShiftService;
import com.seshop.shared.api.PageResponse;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

@WebMvcTest(controllers = {
        StaffInventoryController.class,
        ReceiptController.class,
        ShiftController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = "seshop.cors.allowed-origins=http://localhost:5173")
class ApiControllerContractTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private ReceiptService receiptService;

    @MockBean
    private ShiftService shiftService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpJwtAuthentication() {
        List<String> permissions = List.of(
                "inventory.adjust",
                "inventory.transfer",
                "order.read"
        );
        AuthenticatedUser principal = new AuthenticatedUser(42L, "staff.user", "STAFF", permissions);
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                STAFF_TOKEN,
                authorities
        );

        given(jwtTokenProvider.validate(STAFF_TOKEN)).willReturn(true);
        given(jwtTokenProvider.authentication(STAFF_TOKEN)).willReturn(authentication);
    }

    @Test
    void unauthenticatedStaffEndpointReturnsStandardAuthError() throws Exception {
        mockMvc.perform(get("/api/v1/staff/inventory/balances")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(TraceIdFilter.TRACE_HEADER, "trace-auth"))
                .andExpect(jsonPath("$.code").value("AUTH_001"))
                .andExpect(jsonPath("$.message").value("Authentication required"))
                .andExpect(jsonPath("$.traceId").value("trace-auth"));
    }

    @Test
    void listInventoryBalancesUsesApiEnvelopeAndQueryParameters() throws Exception {
        InventoryBalanceDto balance = new InventoryBalanceDto();
        balance.setId(8801L);
        balance.setLocationId(11L);
        balance.setLocationName("Main Store");
        balance.setVariantId(7001L);
        balance.setSkuCode("SKU-001");
        balance.setProductName("Linen Shirt");
        balance.setOnHandQty(16);
        balance.setReservedQty(1);
        balance.setAvailableQty(15);

        given(inventoryService.listBalances(null, null, "SKU-001", 0, 10))
                .willReturn(new PageResponse<>(List.of(balance), 0, 10, 1, 1));

        mockMvc.perform(get("/api/v1/staff/inventory/balances")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-inventory")
                        .param("skuCode", "SKU-001")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.TRACE_HEADER, "trace-inventory"))
                .andExpect(jsonPath("$.data.items[0].skuCode").value("SKU-001"))
                .andExpect(jsonPath("$.data.items[0].availableQty").value(15))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.meta.traceId").value("trace-inventory"));
    }

    @Test
    void createInventoryTransferUsesAuthenticatedStaffId() throws Exception {
        given(inventoryService.createTransfer(any(CreateTransferRequest.class), eq(42L)))
                .willReturn(9001L);

        mockMvc.perform(post("/api/v1/staff/inventory/transfers")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceLocationId": 11,
                                  "destinationLocationId": 12,
                                  "items": [
                                    {
                                      "variantId": 7001,
                                      "qty": 3
                                    }
                                  ],
                                  "reason": "Rebalancing stock"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transferId").value(9001));

        ArgumentCaptor<CreateTransferRequest> requestCaptor = ArgumentCaptor.forClass(CreateTransferRequest.class);
        then(inventoryService).should().createTransfer(requestCaptor.capture(), eq(42L));
        CreateTransferRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getSourceLocationId()).isEqualTo(11L);
        assertThat(capturedRequest.getDestinationLocationId()).isEqualTo(12L);
        assertThat(capturedRequest.getItems()).hasSize(1);
        assertThat(capturedRequest.getItems().getFirst().getVariantId()).isEqualTo(7001L);
        assertThat(capturedRequest.getItems().getFirst().getQty()).isEqualTo(3);
    }

    @Test
    void createPosReceiptUsesApiEnvelopeAndAuthenticatedStaffId() throws Exception {
        ProcessPosSaleResponse response = new ProcessPosSaleResponse();
        response.setReceiptId(501L);
        response.setReceiptNumber("POS-12345678");
        response.setChangeDue(new BigDecimal("10000.00"));

        given(receiptService.createReceipt(any(ProcessPosSaleRequest.class), eq(42L)))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/pos/receipts")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "shiftId": 501,
                                  "paymentMethod": "CASH",
                                  "amountPaid": 600000,
                                  "items": [
                                    {
                                      "variantId": 7001,
                                      "qty": 1
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.receiptId").value(501))
                .andExpect(jsonPath("$.data.receiptNumber").value("POS-12345678"))
                .andExpect(jsonPath("$.data.changeDue").value(10000.00))
                .andExpect(jsonPath("$.meta.traceId").value("trace-pos"));

        then(receiptService).should().createReceipt(any(ProcessPosSaleRequest.class), eq(42L));
    }

    @Test
    void invalidPosReceiptPayloadReturnsStandardValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/pos/receipts")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invalid-pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentMethod": null,
                                  "amountPaid": -1,
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_001"))
                .andExpect(jsonPath("$.message").value("Invalid request payload"))
                .andExpect(jsonPath("$.traceId").value("trace-invalid-pos"))
                .andExpect(jsonPath("$.details[*].field", hasItems("paymentMethod", "amountPaid", "items")));
    }

    @Test
    void closeShiftReturnsShiftSummary() throws Exception {
        ShiftDto shift = new ShiftDto();
        shift.setId(501L);
        shift.setStaffId(42L);
        shift.setLocationId(11L);
        shift.setStatus("CLOSED");
        shift.setEndingCash(new BigDecimal("2500000.00"));

        given(shiftService.closeShift(eq(501L), any(CloseShiftRequest.class))).willReturn(shift);

        mockMvc.perform(post("/api/v1/pos/shifts/501/close")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-shift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actualCash": 2500000,
                                  "reason": "End of day close"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(501))
                .andExpect(jsonPath("$.data.status").value("CLOSED"))
                .andExpect(jsonPath("$.data.endingCash").value(2500000.00));
    }

    private String bearerToken() {
        return "Bearer " + STAFF_TOKEN;
    }
}
