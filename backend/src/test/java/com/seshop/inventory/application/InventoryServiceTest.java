package com.seshop.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentResponse;
import com.seshop.inventory.api.dto.ReceiveTransferRequest;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.inventory.infrastructure.persistence.InventoryTransferEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryTransferItemEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryTransferRepository;
import com.seshop.inventory.infrastructure.persistence.LocationEntity;
import com.seshop.inventory.infrastructure.persistence.LocationRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryBalanceRepository balanceRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private InventoryTransferRepository transferRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private AuditService auditService;

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(
                balanceRepository,
                locationRepository,
                transferRepository,
                productVariantRepository,
                auditService
        );
    }

    @Test
    void adjustInventoryRequiresOverrideBeforeAvailableStockCanGoNegative() {
        InventoryAdjustmentRequest request = adjustmentRequest(7001L, 11L, -2, "SHRINKAGE", "Cycle count");
        InventoryBalanceEntity balance = balance(8801L, 7001L, location(11L), 1, 0);

        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant(7001L)));
        given(locationRepository.findById(11L)).willReturn(Optional.of(balance.getLocation()));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));

        assertThatThrownBy(() -> service.adjustInventory(request, false))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Missing permission: inventory.adjust.override");

        assertThat(balance.getOnHandQty()).isEqualTo(1);
        then(balanceRepository).should(never()).save(any());
        then(auditService).shouldHaveNoInteractions();
    }

    @Test
    void adjustInventoryWritesBeforeAfterAuditMetadata() {
        InventoryAdjustmentRequest request = adjustmentRequest(7001L, 11L, -2, " SHRINKAGE ", "Cycle count");
        InventoryBalanceEntity balance = balance(8801L, 7001L, location(11L), 5, 2);

        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant(7001L)));
        given(locationRepository.findById(11L)).willReturn(Optional.of(balance.getLocation()));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));
        given(balanceRepository.save(balance)).willReturn(balance);

        InventoryAdjustmentResponse response = service.adjustInventory(request, false);

        assertThat(response.getOnHandQty()).isEqualTo(3);
        assertThat(response.getReservedQty()).isEqualTo(2);
        assertThat(response.getAvailableQty()).isEqualTo(1);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.INVENTORY_ADJUSTED),
                eq("InventoryBalance"),
                eq("8801"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("variantId", 7001L)
                .containsEntry("locationId", 11L)
                .containsEntry("deltaQty", -2)
                .containsEntry("reasonCode", "SHRINKAGE")
                .containsEntry("beforeOnHandQty", 5)
                .containsEntry("afterOnHandQty", 3)
                .containsEntry("beforeAvailableQty", 3)
                .containsEntry("afterAvailableQty", 1)
                .containsEntry("negativeStockOverrideUsed", false);
    }

    @Test
    void adjustInventoryAllowsNegativeAvailableStockWithOverrideAndAuditsUse() {
        InventoryAdjustmentRequest request = adjustmentRequest(7001L, 11L, -3, "OVERRIDE", null);
        InventoryBalanceEntity balance = balance(8801L, 7001L, location(11L), 1, 0);

        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant(7001L)));
        given(locationRepository.findById(11L)).willReturn(Optional.of(balance.getLocation()));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));
        given(balanceRepository.save(balance)).willReturn(balance);

        InventoryAdjustmentResponse response = service.adjustInventory(request, true);

        assertThat(response.getOnHandQty()).isEqualTo(-2);
        assertThat(response.getAvailableQty()).isEqualTo(-2);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.INVENTORY_ADJUSTED),
                eq("InventoryBalance"),
                eq("8801"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("afterAvailableQty", -2)
                .containsEntry("negativeStockOverrideUsed", true);
    }

    @Test
    void approveTransferDecrementsSourceStockAndAuditsStatusTransition() {
        LocationEntity source = location(11L);
        LocationEntity destination = location(12L);
        InventoryTransferEntity transfer = transfer(9001L, "DRAFT", source, destination, 7001L, 3);
        InventoryBalanceEntity balance = balance(8801L, 7001L, source, 5, 1);

        given(transferRepository.findById(9001L)).willReturn(Optional.of(transfer));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));
        given(transferRepository.save(transfer)).willReturn(transfer);

        service.approveTransfer(9001L);

        assertThat(transfer.getStatus()).isEqualTo("IN_TRANSIT");
        assertThat(balance.getOnHandQty()).isEqualTo(2);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.INVENTORY_TRANSFER_CHANGED),
                eq("InventoryTransfer"),
                eq("9001"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("fromStatus", "DRAFT")
                .containsEntry("toStatus", "IN_TRANSIT")
                .containsEntry("sourceLocationId", 11L)
                .containsEntry("destinationLocationId", 12L);
        assertThat((List<?>) metadataCaptor.getValue().get("items")).hasSize(1);
    }

    @Test
    void cancelInTransitTransferRestoresSourceStockAndAuditsCancellation() {
        LocationEntity source = location(11L);
        LocationEntity destination = location(12L);
        InventoryTransferEntity transfer = transfer(9001L, "IN_TRANSIT", source, destination, 7001L, 3);
        InventoryBalanceEntity balance = balance(8801L, 7001L, source, 2, 0);

        given(transferRepository.findById(9001L)).willReturn(Optional.of(transfer));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L))
                .willReturn(Optional.of(balance));
        given(transferRepository.save(transfer)).willReturn(transfer);

        service.cancelTransfer(9001L);

        assertThat(transfer.getStatus()).isEqualTo("CANCELLED");
        assertThat(balance.getOnHandQty()).isEqualTo(5);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.INVENTORY_TRANSFER_CHANGED),
                eq("InventoryTransfer"),
                eq("9001"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("fromStatus", "IN_TRANSIT")
                .containsEntry("toStatus", "CANCELLED");
        assertThat((List<?>) metadataCaptor.getValue().get("restoredItems")).hasSize(1);
    }

    @Test
    void receiveTransferRejectsQuantitiesAboveTransferQuantity() {
        LocationEntity source = location(11L);
        LocationEntity destination = location(12L);
        InventoryTransferEntity transfer = transfer(9001L, "IN_TRANSIT", source, destination, 7001L, 3);
        ReceiveTransferRequest request = receiveRequest(7001L, 2, 2);

        given(transferRepository.findById(9001L)).willReturn(Optional.of(transfer));

        assertThatThrownBy(() -> service.receiveTransfer(9001L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Received plus damaged quantity cannot exceed transfer quantity");

        then(balanceRepository).shouldHaveNoInteractions();
        then(transferRepository).should(never()).save(any());
        then(auditService).shouldHaveNoInteractions();
    }

    @Test
    void receiveTransferUpdatesDestinationStockAndAuditsCompletion() {
        LocationEntity source = location(11L);
        LocationEntity destination = location(12L);
        InventoryTransferEntity transfer = transfer(9001L, "IN_TRANSIT", source, destination, 7001L, 3);
        InventoryBalanceEntity destinationBalance = balance(8802L, 7001L, destination, 7, 0);
        ReceiveTransferRequest request = receiveRequest(7001L, 2, 1);

        given(transferRepository.findById(9001L)).willReturn(Optional.of(transfer));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 12L))
                .willReturn(Optional.of(destinationBalance));
        given(transferRepository.save(transfer)).willReturn(transfer);

        service.receiveTransfer(9001L, request);

        assertThat(transfer.getStatus()).isEqualTo("COMPLETED");
        assertThat(destinationBalance.getOnHandQty()).isEqualTo(9);
        assertThat(transfer.getItems().getFirst().getReceivedQty()).isEqualTo(2);
        assertThat(transfer.getItems().getFirst().getDamagedQty()).isEqualTo(1);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.INVENTORY_TRANSFER_CHANGED),
                eq("InventoryTransfer"),
                eq("9001"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("fromStatus", "IN_TRANSIT")
                .containsEntry("toStatus", "COMPLETED");
        assertThat((List<?>) metadataCaptor.getValue().get("items")).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }

    private InventoryAdjustmentRequest adjustmentRequest(
            Long variantId,
            Long locationId,
            int deltaQty,
            String reasonCode,
            String notes) {
        InventoryAdjustmentRequest request = new InventoryAdjustmentRequest();
        request.setVariantId(variantId);
        request.setLocationId(locationId);
        request.setDeltaQty(deltaQty);
        request.setReasonCode(reasonCode);
        request.setNotes(notes);
        return request;
    }

    private ReceiveTransferRequest receiveRequest(Long variantId, int receivedQty, int damagedQty) {
        ReceiveTransferRequest.ReceivedItemDto item = new ReceiveTransferRequest.ReceivedItemDto();
        item.setVariantId(variantId);
        item.setReceivedQty(receivedQty);
        item.setDamagedQty(damagedQty);

        ReceiveTransferRequest request = new ReceiveTransferRequest();
        request.setReceivedItems(List.of(item));
        return request;
    }

    private ProductVariantEntity variant(Long id) {
        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setId(id);
        variant.setSkuCode("SKU-" + id);
        return variant;
    }

    private InventoryTransferEntity transfer(
            Long id,
            String status,
            LocationEntity source,
            LocationEntity destination,
            Long variantId,
            int qty) {
        InventoryTransferEntity transfer = new InventoryTransferEntity();
        transfer.setId(id);
        transfer.setSourceLocation(source);
        transfer.setDestinationLocation(destination);
        transfer.setStatus(status);
        transfer.setCreatedBy(42L);

        InventoryTransferItemEntity item = new InventoryTransferItemEntity();
        item.setTransfer(transfer);
        item.setVariantId(variantId);
        item.setQty(qty);
        transfer.getItems().add(item);
        return transfer;
    }

    private InventoryBalanceEntity balance(Long id, Long variantId, LocationEntity location, int onHandQty, int reservedQty) {
        InventoryBalanceEntity balance = new InventoryBalanceEntity();
        balance.setId(id);
        balance.setVariantId(variantId);
        balance.setLocation(location);
        balance.setOnHandQty(onHandQty);
        balance.setReservedQty(reservedQty);
        return balance;
    }

    private LocationEntity location(Long id) {
        LocationEntity location = new LocationEntity();
        location.setId(id);
        location.setDisplayName("Location " + id);
        location.setCode("LOC-" + id);
        location.setLocationType("STORE");
        location.setStatus("ACTIVE");
        return location;
    }
}
