package com.seshop.pos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.inventory.infrastructure.persistence.LocationEntity;
import com.seshop.pos.api.dto.ProcessReturnRequest;
import com.seshop.pos.api.dto.ReturnDto;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptItemEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosReturnEntity;
import com.seshop.pos.infrastructure.persistence.PosReturnItemRepository;
import com.seshop.pos.infrastructure.persistence.PosReturnRepository;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock
    private PosReturnRepository returnRepository;

    @Mock
    private PosReturnItemRepository returnItemRepository;

    @Mock
    private PosReceiptRepository receiptRepository;

    @Mock
    private InventoryBalanceRepository balanceRepository;

    @Mock
    private AuditService auditService;

    @Test
    void processReturnValidatesReceiptRestocksAndWritesAuditEvent() {
        ReturnService service = service();
        PosReceiptEntity receipt = receipt(501L, 11L, receiptItem(7001L, 2, "590000.00"));
        InventoryBalanceEntity balance = balance(7001L, 11L, 4, 0);
        ProcessReturnRequest request = returnRequest("RESTOCK");

        given(receiptRepository.findById(501L)).willReturn(Optional.of(receipt));
        given(returnItemRepository.sumReturnedQtyByReceiptIdAndVariantId(501L, 7001L)).willReturn(0L);
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L)).willReturn(Optional.of(balance));
        given(returnRepository.save(any(PosReturnEntity.class))).willAnswer(invocation -> {
            PosReturnEntity entity = invocation.getArgument(0);
            entity.setId(700L);
            entity.setProcessedAt(OffsetDateTime.now());
            return entity;
        });

        ReturnDto dto = service.processReturn(request, 42L);

        assertThat(dto.getId()).isEqualTo(700L);
        assertThat(dto.getOriginalReceiptId()).isEqualTo(501L);
        assertThat(dto.getOriginalOrderId()).isEqualTo(501L);
        assertThat(dto.getRefundAmount()).isEqualByComparingTo("590000.00");
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().getFirst().getDisposition()).isEqualTo("RESTOCK");
        assertThat(balance.getOnHandQty()).isEqualTo(5);

        ArgumentCaptor<PosReturnEntity> returnCaptor = ArgumentCaptor.forClass(PosReturnEntity.class);
        then(returnRepository).should().save(returnCaptor.capture());
        PosReturnEntity savedReturn = returnCaptor.getValue();
        assertThat(savedReturn.getOriginalReceiptId()).isEqualTo(501L);
        assertThat(savedReturn.getOriginalOrderId()).isNull();
        assertThat(savedReturn.getItems()).hasSize(1);
        assertThat(savedReturn.getItems().getFirst().getVariantId()).isEqualTo(7001L);
        assertThat(savedReturn.getItems().getFirst().getQty()).isEqualTo(1);
        then(balanceRepository).should().save(balance);

        ArgumentCaptor<Map<String, Object>> metadataCaptor = metadataCaptor();
        then(auditService).should().write(
                eq(AuditAction.POS_RETURN_PROCESSED),
                eq("PosReturn"),
                eq("700"),
                metadataCaptor.capture()
        );
        assertThat(metadataCaptor.getValue())
                .containsEntry("posReturnId", 700L)
                .containsEntry("originalReceiptId", 501L)
                .containsEntry("processedBy", 42L)
                .containsEntry("refundAmount", new BigDecimal("590000.00"))
                .containsEntry("refundableAmount", new BigDecimal("590000.00"));
        assertThat((List<?>) metadataCaptor.getValue().get("items")).hasSize(1);
    }

    @Test
    void processReturnRejectsQuantityBeyondReceiptRemainingQty() {
        ReturnService service = service();
        PosReceiptEntity receipt = receipt(501L, 11L, receiptItem(7001L, 2, "590000.00"));
        given(receiptRepository.findById(501L)).willReturn(Optional.of(receipt));
        given(returnItemRepository.sumReturnedQtyByReceiptIdAndVariantId(501L, 7001L)).willReturn(2L);

        assertThatThrownBy(() -> service.processReturn(returnRequest("RESTOCK"), 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("quantity exceeds refundable quantity");
    }

    @Test
    void processReturnRejectsRefundAmountAboveReturnedItemValue() {
        ReturnService service = service();
        PosReceiptEntity receipt = receipt(501L, 11L, receiptItem(7001L, 1, "590000.00"));
        ProcessReturnRequest request = returnRequest("DISPOSE");
        request.setRefundAmount(new BigDecimal("590001.00"));
        given(receiptRepository.findById(501L)).willReturn(Optional.of(receipt));
        given(returnItemRepository.sumReturnedQtyByReceiptIdAndVariantId(501L, 7001L)).willReturn(0L);

        assertThatThrownBy(() -> service.processReturn(request, 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refund amount must equal returned item value");
    }

    @Test
    void processReturnRejectsInvalidDisposition() {
        ReturnService service = service();
        PosReceiptEntity receipt = receipt(501L, 11L, receiptItem(7001L, 1, "590000.00"));
        given(receiptRepository.findById(501L)).willReturn(Optional.of(receipt));

        assertThatThrownBy(() -> service.processReturn(returnRequest("SELL_AGAIN"), 42L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid return disposition");
    }

    private ReturnService service() {
        return new ReturnService(
                returnRepository,
                returnItemRepository,
                receiptRepository,
                balanceRepository,
                auditService
        );
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<Map<String, Object>> metadataCaptor() {
        return ArgumentCaptor.forClass(Map.class);
    }

    private ProcessReturnRequest returnRequest(String disposition) {
        ProcessReturnRequest.Item item = new ProcessReturnRequest.Item();
        item.setVariantId(7001L);
        item.setQty(1);
        item.setDisposition(disposition);

        ProcessReturnRequest request = new ProcessReturnRequest();
        request.setOriginalOrderId(501L);
        request.setRefundAmount(new BigDecimal("590000.00"));
        request.setReason("Customer return");
        request.setItems(List.of(item));
        return request;
    }

    private PosReceiptEntity receipt(Long id, Long locationId, PosReceiptItemEntity item) {
        PosShiftEntity shift = new PosShiftEntity();
        shift.setId(101L);
        shift.setStaffId(42L);
        shift.setLocationId(locationId);
        shift.setStatus("OPEN");

        PosReceiptEntity receipt = new PosReceiptEntity();
        receipt.setId(id);
        receipt.setShift(shift);
        receipt.setPaymentMethod("CASH");
        receipt.setTotalAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())));
        item.setReceipt(receipt);
        receipt.getItems().add(item);
        return receipt;
    }

    private PosReceiptItemEntity receiptItem(Long variantId, int qty, String unitPrice) {
        PosReceiptItemEntity item = new PosReceiptItemEntity();
        item.setId(800L);
        item.setVariantId(variantId);
        item.setQty(qty);
        item.setUnitPrice(new BigDecimal(unitPrice));
        return item;
    }

    private InventoryBalanceEntity balance(Long variantId, Long locationId, int onHandQty, int reservedQty) {
        LocationEntity location = new LocationEntity();
        location.setId(locationId);
        location.setDisplayName("Store");

        InventoryBalanceEntity balance = new InventoryBalanceEntity();
        balance.setId(8801L);
        balance.setVariantId(variantId);
        balance.setLocation(location);
        balance.setOnHandQty(onHandQty);
        balance.setReservedQty(reservedQty);
        return balance;
    }
}
