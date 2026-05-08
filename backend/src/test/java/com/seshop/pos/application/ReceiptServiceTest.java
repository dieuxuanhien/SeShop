package com.seshop.pos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.seshop.catalog.infrastructure.persistence.ProductEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.inventory.infrastructure.persistence.LocationEntity;
import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private PosReceiptRepository receiptRepository;

    @Mock
    private PosShiftRepository shiftRepository;

    @Mock
    private com.seshop.catalog.infrastructure.persistence.ProductVariantRepository productVariantRepository;

    @Mock
    private InventoryBalanceRepository balanceRepository;

    @Test
    void createReceiptDecrementsInventoryAtShiftLocation() {
        ReceiptService service = new ReceiptService(
                receiptRepository,
                shiftRepository,
                productVariantRepository,
                balanceRepository
        );

        PosShiftEntity shift = new PosShiftEntity();
        shift.setId(501L);
        shift.setStaffId(42L);
        shift.setLocationId(11L);
        shift.setStatus("OPEN");

        ProductVariantEntity variant = variant(7001L, "SKU-001", "Linen Shirt", "590000.00");
        InventoryBalanceEntity balance = balance(8801L, 7001L, 11L, 5, 1);

        given(shiftRepository.findByStaffIdAndStatus(42L, "OPEN")).willReturn(Optional.of(shift));
        given(productVariantRepository.findById(7001L)).willReturn(Optional.of(variant));
        given(balanceRepository.findForUpdateByVariantIdAndLocationId(7001L, 11L)).willReturn(Optional.of(balance));
        given(receiptRepository.save(any(PosReceiptEntity.class))).willAnswer(invocation -> {
            PosReceiptEntity receipt = invocation.getArgument(0);
            receipt.setId(9001L);
            return receipt;
        });

        ProcessPosSaleRequest request = new ProcessPosSaleRequest();
        request.setPaymentMethod("CASH");
        request.setAmountPaid(new BigDecimal("600000.00"));
        ProcessPosSaleRequest.Item item = new ProcessPosSaleRequest.Item();
        item.setVariantId(7001L);
        item.setQty(1);
        request.setItems(List.of(item));

        ProcessPosSaleResponse response = service.createReceipt(request, 42L);

        assertThat(response.getReceiptId()).isEqualTo(9001L);
        assertThat(response.getReceiptNumber()).isEqualTo("POS-00009001");
        assertThat(response.getChangeDue()).isEqualByComparingTo("10000.00");
        assertThat(balance.getOnHandQty()).isEqualTo(4);

        ArgumentCaptor<PosReceiptEntity> receiptCaptor = ArgumentCaptor.forClass(PosReceiptEntity.class);
        then(receiptRepository).should().save(receiptCaptor.capture());
        PosReceiptEntity savedReceipt = receiptCaptor.getValue();
        assertThat(savedReceipt.getShift()).isSameAs(shift);
        assertThat(savedReceipt.getTotalAmount()).isEqualByComparingTo("590000.00");
        assertThat(savedReceipt.getPaymentMethod()).isEqualTo("CASH");
        assertThat(savedReceipt.getItems()).hasSize(1);
        assertThat(savedReceipt.getItems().getFirst().getUnitPrice()).isEqualByComparingTo("590000.00");
        then(balanceRepository).should().save(balance);
    }

    private ProductVariantEntity variant(Long id, String skuCode, String productName, String price) {
        ProductEntity product = new ProductEntity();
        product.setName(productName);

        ProductVariantEntity variant = new ProductVariantEntity();
        variant.setId(id);
        variant.setSkuCode(skuCode);
        variant.setProduct(product);
        variant.setPrice(new BigDecimal(price));
        return variant;
    }

    private InventoryBalanceEntity balance(Long id, Long variantId, Long locationId, int onHandQty, int reservedQty) {
        LocationEntity location = new LocationEntity();
        location.setId(locationId);
        location.setDisplayName("Store");

        InventoryBalanceEntity balance = new InventoryBalanceEntity();
        balance.setId(id);
        balance.setVariantId(variantId);
        balance.setLocation(location);
        balance.setOnHandQty(onHandQty);
        balance.setReservedQty(reservedQty);
        return balance;
    }
}
