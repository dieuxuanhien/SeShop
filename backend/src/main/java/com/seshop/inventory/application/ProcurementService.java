package com.seshop.inventory.application;

import com.seshop.inventory.api.dto.CreatePurchaseOrderRequest;
import com.seshop.inventory.api.dto.GoodsReceiptRequest;
import com.seshop.inventory.api.dto.GoodsReceiptResponse;
import com.seshop.inventory.api.dto.PurchaseOrderResponse;
import com.seshop.inventory.infrastructure.persistence.*;
import com.seshop.shared.exception.ResourceNotFoundException;
import com.seshop.shared.exception.SeShopValidationException;
import com.seshop.shared.util.DateTimeUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@Service
@Transactional
public class ProcurementService {

    private static final String PO_STATUS_OPEN = "OPEN";
    private static final String PO_STATUS_RECEIVED = "RECEIVED";
    private static final String RECEIPT_STATUS_RECEIVED = "RECEIVED";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final LocationRepository locationRepository;
    private final InventoryBalanceRepository balanceRepository;

    public ProcurementService(PurchaseOrderRepository purchaseOrderRepository,
                              GoodsReceiptRepository goodsReceiptRepository,
                              LocationRepository locationRepository,
                              InventoryBalanceRepository balanceRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.locationRepository = locationRepository;
        this.balanceRepository = balanceRepository;
    }

    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request, Long createdBy) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new SeShopValidationException("Items list cannot be empty");
        }

        Long destinationLocationId = Objects.requireNonNull(request.getDestinationLocationId(),
                "Destination location ID is required");

        locationRepository.findById(destinationLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_404", "Destination location not found"));

        PurchaseOrderEntity purchaseOrder = new PurchaseOrderEntity();
        purchaseOrder.setPoNumber(generatePoNumber());
        purchaseOrder.setSupplierId(request.getSupplierId());
        purchaseOrder.setDestinationLocationId(destinationLocationId);
        purchaseOrder.setStatus(PO_STATUS_OPEN);
        purchaseOrder.setCreatedBy(createdBy);

        for (CreatePurchaseOrderRequest.PurchaseOrderItemDto itemDto : request.getItems()) {
            PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
            item.setPurchaseOrder(purchaseOrder);
            item.setVariantId(itemDto.getVariantId());
            item.setOrderedQty(itemDto.getOrderedQty());
            item.setUnitCost(itemDto.getUnitCost());
            purchaseOrder.getItems().add(item);
        }

        PurchaseOrderEntity saved = purchaseOrderRepository.save(purchaseOrder);
        return mapToResponse(saved);
    }

    public GoodsReceiptResponse createGoodsReceipt(GoodsReceiptRequest request, Long receivedBy) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new SeShopValidationException("Items list cannot be empty");
        }

        Long purchaseOrderId = Objects.requireNonNull(request.getPurchaseOrderId(),
            "Purchase order ID is required");

        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_404", "Purchase order not found"));

        Long destinationLocationId = Objects.requireNonNull(purchaseOrder.getDestinationLocationId(),
            "Destination location ID is required");

        LocationEntity destination = locationRepository.findById(destinationLocationId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_404", "Destination location not found"));

        GoodsReceiptEntity receipt = new GoodsReceiptEntity();
        receipt.setPurchaseOrderId(purchaseOrder.getId());
        receipt.setReceivedBy(receivedBy);
        receipt.setReceivedAt(resolveReceivedAt(request.getReceivedAt()));
        receipt.setStatus(RECEIPT_STATUS_RECEIVED);

        request.getItems().forEach(itemDto -> {
            GoodsReceiptItemEntity item = new GoodsReceiptItemEntity();
            item.setGoodsReceipt(receipt);
            item.setVariantId(itemDto.getVariantId());
            item.setReceivedQty(itemDto.getReceivedQty());
            item.setDamagedQty(itemDto.getDamagedQty() == null ? 0 : itemDto.getDamagedQty());
            receipt.getItems().add(item);

            InventoryBalanceEntity balance = balanceRepository
                    .findByVariantIdAndLocationId(itemDto.getVariantId(), destination.getId())
                    .orElseGet(() -> {
                        InventoryBalanceEntity newBalance = new InventoryBalanceEntity();
                        newBalance.setVariantId(itemDto.getVariantId());
                        newBalance.setLocation(destination);
                        newBalance.setOnHandQty(0);
                        newBalance.setReservedQty(0);
                        return newBalance;
                    });

            balance.setOnHandQty(balance.getOnHandQty() + itemDto.getReceivedQty());
            balanceRepository.save(balance);
        });

        purchaseOrder.setStatus(PO_STATUS_RECEIVED);
        purchaseOrderRepository.save(purchaseOrder);

        GoodsReceiptEntity saved = goodsReceiptRepository.save(receipt);
        return mapToResponse(saved);
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrderEntity entity) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(entity.getId());
        response.setPoNumber(entity.getPoNumber());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setSupplierId(entity.getSupplierId());
        response.setDestinationLocationId(entity.getDestinationLocationId());
        return response;
    }

    private GoodsReceiptResponse mapToResponse(GoodsReceiptEntity entity) {
        GoodsReceiptResponse response = new GoodsReceiptResponse();
        response.setId(entity.getId());
        response.setPurchaseOrderId(entity.getPurchaseOrderId());
        response.setStatus(entity.getStatus());
        response.setReceivedAt(entity.getReceivedAt());
        return response;
    }

    private String generatePoNumber() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT)
                .format(OffsetDateTime.now());
        String suffix = Integer.toString((int) (Math.random() * 9000) + 1000);
        return "PO-" + timestamp + "-" + suffix;
    }

    private OffsetDateTime resolveReceivedAt(OffsetDateTime receivedAt) {
        return receivedAt != null ? receivedAt : DateTimeUtils.utcNow();
    }
}
