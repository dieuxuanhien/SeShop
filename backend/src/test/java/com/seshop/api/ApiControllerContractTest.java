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
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentResponse;
import com.seshop.inventory.api.dto.InventoryBalanceDto;
import com.seshop.inventory.application.InventoryService;
import com.seshop.pos.api.ReceiptController;
import com.seshop.pos.api.ReturnController;
import com.seshop.pos.api.ShiftController;
import com.seshop.pos.api.dto.CloseShiftRequest;
import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.api.dto.ProcessReturnRequest;
import com.seshop.pos.api.dto.ReturnDto;
import com.seshop.pos.api.dto.ShiftDto;
import com.seshop.pos.application.ReceiptService;
import com.seshop.pos.application.ReturnService;
import com.seshop.pos.application.ShiftService;
import com.seshop.shared.api.PageResponse;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.LocationAccessService;
import com.seshop.shared.security.LocationScope;
import com.seshop.shared.security.PermissionValidator;
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
        ReturnController.class,
        ShiftController.class
})
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
class ApiControllerContractTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private ReceiptService receiptService;

    @MockBean
    private ReturnService returnService;

    @MockBean
    private ShiftService shiftService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private LocationAccessService locationAccessService;

    @BeforeEach
    void setUpJwtAuthentication() {
        List<String> permissions = List.of(
                "inventory.adjust",
                "inventory.transfer",
                "order.read",
                "pos.sell",
                "pos.shift.manage",
                "refund.process"
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
        given(locationAccessService.scopeFor(any())).willReturn(LocationScope.all());
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
    void actuatorPrometheusEndpointIsNotBlockedByAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-prometheus"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string(TraceIdFilter.TRACE_HEADER, "trace-prometheus"))
                .andExpect(jsonPath("$.code").value("GEN_500"));
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

        given(inventoryService.listBalances(eq(null), eq(null), eq("SKU-001"), eq(0), eq(10), any()))
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
    void adjustInventoryPassesNegativeOverridePermissionFlag() throws Exception {
        String token = "inventory-manager-token";
        List<String> permissions = List.of("inventory.adjust", "inventory.adjust.override");
        AuthenticatedUser principal = new AuthenticatedUser(43L, "inventory.manager", "STAFF", permissions);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(authentication);

        InventoryAdjustmentResponse response = new InventoryAdjustmentResponse();
        response.setInventoryBalanceId(8801L);
        response.setOnHandQty(-1);
        response.setReservedQty(0);
        response.setAvailableQty(-1);

        given(inventoryService.adjustInventory(any(InventoryAdjustmentRequest.class), eq(true), any()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/staff/inventory/adjustments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "variantId": 7001,
                                  "locationId": 11,
                                  "deltaQty": -10,
                                  "reasonCode": "OVERRIDE",
                                  "notes": "Manager-approved correction"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.inventoryBalanceId").value(8801))
                .andExpect(jsonPath("$.data.availableQty").value(-1));

        ArgumentCaptor<InventoryAdjustmentRequest> requestCaptor =
                ArgumentCaptor.forClass(InventoryAdjustmentRequest.class);
        then(inventoryService).should().adjustInventory(requestCaptor.capture(), eq(true), any());
        assertThat(requestCaptor.getValue().getReasonCode()).isEqualTo("OVERRIDE");
    }

    @Test
    void invalidInventoryAdjustmentPayloadReturnsReasonCodeError() throws Exception {
        mockMvc.perform(post("/api/v1/staff/inventory/adjustments")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-invalid-adjustment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "variantId": 7001,
                                  "locationId": 11,
                                  "deltaQty": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_001"))
                .andExpect(jsonPath("$.details[*].field", hasItems("reasonCode")));
    }

    @Test
    void createInventoryTransferUsesAuthenticatedStaffId() throws Exception {
        given(inventoryService.createTransfer(any(CreateTransferRequest.class), eq(42L), any()))
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
        then(inventoryService).should().createTransfer(requestCaptor.capture(), eq(42L), any());
        CreateTransferRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getSourceLocationId()).isEqualTo(11L);
        assertThat(capturedRequest.getDestinationLocationId()).isEqualTo(12L);
        assertThat(capturedRequest.getItems()).hasSize(1);
        assertThat(capturedRequest.getItems().getFirst().getVariantId()).isEqualTo(7001L);
        assertThat(capturedRequest.getItems().getFirst().getQty()).isEqualTo(3);
    }

    @Test
    void cancelInventoryTransferRequiresTransferPermissionAndCallsService() throws Exception {
        mockMvc.perform(post("/api/v1/staff/inventory/transfers/9001/cancel")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-transfer-cancel"))
                .andExpect(status().isOk());

        then(inventoryService).should().cancelTransfer(eq(9001L), any());
    }

    @Test
    void createPosReceiptUsesApiEnvelopeAndAuthenticatedStaffId() throws Exception {
        ProcessPosSaleResponse response = new ProcessPosSaleResponse();
        response.setReceiptId(501L);
        response.setReceiptNumber("POS-12345678");
        response.setChangeDue(new BigDecimal("10000.00"));

        given(receiptService.createReceipt(any(ProcessPosSaleRequest.class), eq(42L), any()))
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

        then(receiptService).should().createReceipt(any(ProcessPosSaleRequest.class), eq(42L), any());
    }

    @Test
    void createPosReceiptRejectsAuthenticatedUserWithoutPosSellPermission() throws Exception {
        String token = "pos-viewer-token";
        List<String> permissions = List.of("order.read");
        AuthenticatedUser principal = new AuthenticatedUser(43L, "staff.viewer", "STAFF", permissions);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(authentication);

        mockMvc.perform(post("/api/v1/pos/receipts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-pos-forbidden")
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
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
    void processPosReturnRequiresRefundPermissionAndPassesAuthenticatedStaffId() throws Exception {
        ReturnDto response = new ReturnDto();
        response.setId(700L);
        response.setOriginalReceiptId(501L);
        response.setOriginalOrderId(501L);
        response.setRefundAmount(new BigDecimal("590000.00"));
        response.setReason("Customer return");

        given(returnService.processReturn(any(ProcessReturnRequest.class), eq(42L), any()))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/pos/returns")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken())
                        .header(TraceIdFilter.TRACE_HEADER, "trace-pos-return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalOrderId": 501,
                                  "refundAmount": 590000,
                                  "reason": "Customer return",
                                  "items": [
                                    {
                                      "variantId": 7001,
                                      "qty": 1,
                                      "disposition": "RESTOCK"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(700))
                .andExpect(jsonPath("$.data.originalReceiptId").value(501))
                .andExpect(jsonPath("$.data.refundAmount").value(590000.00));

        ArgumentCaptor<ProcessReturnRequest> requestCaptor =
                ArgumentCaptor.forClass(ProcessReturnRequest.class);
        then(returnService).should().processReturn(requestCaptor.capture(), eq(42L), any());
        assertThat(requestCaptor.getValue().getOriginalOrderId()).isEqualTo(501L);
        assertThat(requestCaptor.getValue().getItems()).hasSize(1);
        assertThat(requestCaptor.getValue().getItems().getFirst().getDisposition()).isEqualTo("RESTOCK");
    }

    @Test
    void processPosReturnRejectsAuthenticatedUserWithoutRefundPermission() throws Exception {
        String token = "pos-return-viewer-token";
        List<String> permissions = List.of("pos.sell");
        AuthenticatedUser principal = new AuthenticatedUser(43L, "staff.viewer", "STAFF", permissions);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(authentication);

        mockMvc.perform(post("/api/v1/pos/returns")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-pos-return-forbidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalOrderId": 501,
                                  "refundAmount": 590000,
                                  "reason": "Customer return",
                                  "items": [
                                    {
                                      "variantId": 7001,
                                      "qty": 1,
                                      "disposition": "RESTOCK"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
    }

    @Test
    void closeShiftReturnsShiftSummary() throws Exception {
        ShiftDto shift = new ShiftDto();
        shift.setId(501L);
        shift.setStaffId(42L);
        shift.setLocationId(11L);
        shift.setStatus("CLOSED");
        shift.setEndingCash(new BigDecimal("2500000.00"));

        given(shiftService.getShift(501L)).willReturn(shift);
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

    @Test
    void closeShiftRejectsAuthenticatedUserWithoutShiftPermission() throws Exception {
        String token = "shift-viewer-token";
        List<String> permissions = List.of("pos.sell");
        AuthenticatedUser principal = new AuthenticatedUser(43L, "staff.viewer", "STAFF", permissions);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(authentication);

        mockMvc.perform(post("/api/v1/pos/shifts/501/close")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-shift-forbidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actualCash": 2500000,
                                  "reason": "End of day close"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
    }

    private String bearerToken() {
        return "Bearer " + STAFF_TOKEN;
    }
}
