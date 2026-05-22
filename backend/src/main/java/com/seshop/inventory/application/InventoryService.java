package com.seshop.inventory.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.inventory.api.dto.CreateTransferRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentResponse;
import com.seshop.inventory.api.dto.InventoryBalanceDto;
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.LocationAvailabilityDto;
import com.seshop.inventory.api.dto.ProductVariantDto;
import com.seshop.inventory.api.dto.ReceiveTransferRequest;
import com.seshop.inventory.api.dto.StockTransferDto;
import com.seshop.inventory.infrastructure.persistence.*;
import com.seshop.shared.api.PageResponse;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.seshop.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.seshop.shared.security.LocationScope;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private final InventoryBalanceRepository balanceRepository;
    private final LocationRepository locationRepository;
    private final InventoryTransferRepository transferRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AuditService auditService;

    public InventoryService(InventoryBalanceRepository balanceRepository,
                            LocationRepository locationRepository,
                            InventoryTransferRepository transferRepository,
                            ProductVariantRepository productVariantRepository,
                            AuditService auditService) {
        this.balanceRepository = balanceRepository;
        this.locationRepository = locationRepository;
        this.transferRepository = transferRepository;
        this.productVariantRepository = productVariantRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LocationAvailabilityDto> getAvailabilityByVariant(Long variantId) {
        return balanceRepository.findByVariantId(variantId).stream()
                .map(balance -> {
                    LocationAvailabilityDto dto = new LocationAvailabilityDto();
                    dto.setLocationId(balance.getLocation().getId());
                    dto.setLocationName(balance.getLocation().getDisplayName());
                    dto.setAvailableQty(balance.getOnHandQty() - balance.getReservedQty());
                    dto.setUpdatedAt(balance.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationAvailabilityDto> getAvailabilityByProduct(Long productId) {
        Map<Long, LocationAvailabilityDto> byLocation = productVariantRepository.findAll().stream()
                .filter(variant -> variant.getProduct().getId().equals(productId))
                .flatMap(variant -> getAvailabilityByVariant(variant.getId()).stream())
                .collect(Collectors.toMap(
                        LocationAvailabilityDto::getLocationId,
                        location -> location,
                        (left, right) -> {
                            left.setAvailableQty(left.getAvailableQty() + right.getAvailableQty());
                            if (right.getUpdatedAt() != null
                                    && (left.getUpdatedAt() == null || right.getUpdatedAt().isAfter(left.getUpdatedAt()))) {
                                left.setUpdatedAt(right.getUpdatedAt());
                            }
                            return left;
                        }));
        return byLocation.values().stream()
                .sorted((left, right) -> left.getLocationName().compareToIgnoreCase(right.getLocationName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryBalanceDto> listBalances(Long variantId, Long locationId, String skuCode, int page, int size) {
        return listBalances(variantId, locationId, skuCode, page, size, LocationScope.all());
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryBalanceDto> listBalances(
            Long variantId,
            Long locationId,
            String skuCode,
            int page,
            int size,
            LocationScope locationScope) {
        Long resolvedVariantId = variantId;
        if (skuCode != null && !skuCode.isBlank()) {
            resolvedVariantId = productVariantRepository.findBySkuCode(skuCode)
                    .map(ProductVariantEntity::getId)
                    .orElse(-1L);
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<InventoryBalanceEntity> balances = locationScope.isEmpty()
                ? new PageImpl<>(List.of(), pageRequest, 0)
                : locationScope.allLocations()
                        ? balanceRepository.search(resolvedVariantId, locationId, pageRequest)
                        : balanceRepository.searchScoped(
                                resolvedVariantId,
                                locationId,
                                locationScope.locationIds(),
                                pageRequest);

        return new PageResponse<>(
                balances.getContent().stream().map(this::mapBalance).toList(),
                balances.getNumber(),
                balances.getSize(),
                balances.getTotalElements(),
                balances.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<StockTransferDto> listTransfers(int page, int size) {
        return listTransfers(page, size, LocationScope.all());
    }

    @Transactional(readOnly = true)
    public PageResponse<StockTransferDto> listTransfers(int page, int size, LocationScope locationScope) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<InventoryTransferEntity> transfers = locationScope.isEmpty()
                ? new PageImpl<>(List.of(), pageRequest, 0)
                : locationScope.allLocations()
                        ? transferRepository.findAll(pageRequest)
                        : transferRepository.findByLocationScope(locationScope.locationIds(), pageRequest);
        return new PageResponse<>(
                transfers.getContent().stream().map(this::mapTransfer).toList(),
                transfers.getNumber(),
                transfers.getSize(),
                transfers.getTotalElements(),
                transfers.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductVariantDto getProductVariantBySku(String skuCode) {
        ProductVariantEntity variant = productVariantRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new IllegalArgumentException("SKU not found"));
        return mapVariant(variant);
    }

    public InventoryAdjustmentResponse adjustInventory(InventoryAdjustmentRequest request, boolean canOverrideNegativeStock) {
        return adjustInventory(request, canOverrideNegativeStock, LocationScope.all());
    }

    public InventoryAdjustmentResponse adjustInventory(
            InventoryAdjustmentRequest request,
            boolean canOverrideNegativeStock,
            LocationScope locationScope) {
        requireLocationScope(locationScope, request.getLocationId());

        productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("CAT_404", "Variant not found"));

        LocationEntity location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("INV_003", "Location not found"));

        InventoryBalanceEntity balance = balanceRepository
                .findForUpdateByVariantIdAndLocationId(request.getVariantId(), location.getId())
                .orElseGet(() -> {
                    InventoryBalanceEntity newBalance = new InventoryBalanceEntity();
                    newBalance.setVariantId(request.getVariantId());
                    newBalance.setLocation(location);
                    newBalance.setOnHandQty(0);
                    newBalance.setReservedQty(0);
                    return newBalance;
                });

        int beforeOnHandQty = balance.getOnHandQty();
        int beforeReservedQty = balance.getReservedQty();
        int beforeAvailableQty = beforeOnHandQty - beforeReservedQty;
        int afterOnHandQty = beforeOnHandQty + request.getDeltaQty();
        int afterAvailableQty = afterOnHandQty - beforeReservedQty;

        if (afterAvailableQty < 0 && !canOverrideNegativeStock) {
            throw new ForbiddenOperationException("Missing permission: inventory.adjust.override");
        }

        balance.setOnHandQty(afterOnHandQty);
        InventoryBalanceEntity savedBalance = balanceRepository.save(balance);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("variantId", request.getVariantId());
        metadata.put("locationId", location.getId());
        metadata.put("deltaQty", request.getDeltaQty());
        metadata.put("reasonCode", request.getReasonCode().trim());
        metadata.put("notes", request.getNotes());
        metadata.put("beforeOnHandQty", beforeOnHandQty);
        metadata.put("afterOnHandQty", savedBalance.getOnHandQty());
        metadata.put("beforeReservedQty", beforeReservedQty);
        metadata.put("afterReservedQty", savedBalance.getReservedQty());
        metadata.put("beforeAvailableQty", beforeAvailableQty);
        metadata.put("afterAvailableQty", afterAvailableQty);
        metadata.put("negativeStockOverrideUsed", afterAvailableQty < 0);
        auditService.write(
                AuditAction.INVENTORY_ADJUSTED,
                "InventoryBalance",
                savedBalance.getId() == null ? null : savedBalance.getId().toString(),
                metadata);

        return mapAdjustment(savedBalance);
    }

    public Long createTransfer(CreateTransferRequest request, Long createdBy) {
        return createTransfer(request, createdBy, LocationScope.all());
    }

    public Long createTransfer(CreateTransferRequest request, Long createdBy, LocationScope locationScope) {
        requireLocationScope(locationScope, request.getSourceLocationId());

        LocationEntity source = locationRepository.findById(request.getSourceLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("INV_003", "Source location not found"));
        LocationEntity destination = locationRepository.findById(request.getDestinationLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("INV_003", "Destination location not found"));

        if (source.getId().equals(destination.getId())) {
            throw new BusinessException("INV_002", "Source and destination locations must be different");
        }

        InventoryTransferEntity transfer = new InventoryTransferEntity();
        transfer.setSourceLocation(source);
        transfer.setDestinationLocation(destination);
        transfer.setStatus("DRAFT");
        transfer.setCreatedBy(createdBy);

        for (CreateTransferRequest.TransferItemDto itemDto : request.getItems()) {
            productVariantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("CAT_404", "Variant not found"));
            boolean duplicateVariant = transfer.getItems().stream()
                    .anyMatch(item -> item.getVariantId().equals(itemDto.getVariantId()));
            if (duplicateVariant) {
                throw new BusinessException("INV_002", "Transfer cannot contain duplicate variants");
            }
            InventoryTransferItemEntity item = new InventoryTransferItemEntity();
            item.setTransfer(transfer);
            item.setVariantId(itemDto.getVariantId());
            item.setQty(itemDto.getQty());
            transfer.getItems().add(item);
        }

        InventoryTransferEntity saved = transferRepository.save(transfer);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("createdBy", createdBy);
        metadata.put("reason", request.getReason());
        auditTransferStatusChange(saved, null, "DRAFT", metadata);
        return saved.getId();
    }

    public void approveTransfer(Long transferId) {
        approveTransfer(transferId, LocationScope.all());
    }

    public void approveTransfer(Long transferId, LocationScope locationScope) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_002", "Transfer not found"));
        requireLocationScope(locationScope, transfer.getSourceLocation().getId());

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new BusinessException("INV_002", "Transfer must be in DRAFT state to approve");
        }

        List<Map<String, Object>> auditedItems = transfer.getItems().stream()
                .map(item -> {
                    InventoryBalanceEntity balance = balanceRepository
                            .findForUpdateByVariantIdAndLocationId(item.getVariantId(), transfer.getSourceLocation().getId())
                            .orElseThrow(() -> new BusinessException("INV_001", "Source balance not found for variant"));

                    int beforeOnHandQty = balance.getOnHandQty();
                    int beforeReservedQty = balance.getReservedQty();
                    int beforeAvailableQty = beforeOnHandQty - beforeReservedQty;
                    if (beforeAvailableQty < item.getQty()) {
                        throw new BusinessException("INV_001", "Insufficient available stock at source location");
                    }
                    balance.setOnHandQty(beforeOnHandQty - item.getQty());
                    balanceRepository.save(balance);

                    Map<String, Object> itemMetadata = new LinkedHashMap<>();
                    itemMetadata.put("variantId", item.getVariantId());
                    itemMetadata.put("qty", item.getQty());
                    itemMetadata.put("locationId", transfer.getSourceLocation().getId());
                    itemMetadata.put("beforeOnHandQty", beforeOnHandQty);
                    itemMetadata.put("afterOnHandQty", balance.getOnHandQty());
                    itemMetadata.put("beforeReservedQty", beforeReservedQty);
                    itemMetadata.put("afterReservedQty", balance.getReservedQty());
                    itemMetadata.put("beforeAvailableQty", beforeAvailableQty);
                    itemMetadata.put("afterAvailableQty", balance.getOnHandQty() - balance.getReservedQty());
                    return itemMetadata;
                })
                .toList();

        String fromStatus = transfer.getStatus();
        transfer.setStatus("IN_TRANSIT");
        InventoryTransferEntity savedTransfer = transferRepository.save(transfer);
        auditTransferStatusChange(savedTransfer, fromStatus, "IN_TRANSIT", Map.of("items", auditedItems));
    }

    public void cancelTransfer(Long transferId) {
        cancelTransfer(transferId, LocationScope.all());
    }

    public void cancelTransfer(Long transferId, LocationScope locationScope) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_002", "Transfer not found"));
        requireLocationScope(locationScope, transfer.getSourceLocation().getId());

        if (!"DRAFT".equals(transfer.getStatus()) && !"IN_TRANSIT".equals(transfer.getStatus())) {
            throw new BusinessException("INV_002", "Only DRAFT or IN_TRANSIT transfers can be cancelled");
        }

        List<Map<String, Object>> restoredItems = List.of();
        if ("IN_TRANSIT".equals(transfer.getStatus())) {
            restoredItems = transfer.getItems().stream()
                    .map(item -> {
                        InventoryBalanceEntity balance = balanceRepository
                                .findForUpdateByVariantIdAndLocationId(
                                        item.getVariantId(),
                                        transfer.getSourceLocation().getId())
                                .orElseThrow(() -> new BusinessException("INV_001", "Source balance not found for variant"));
                        int beforeOnHandQty = balance.getOnHandQty();
                        balance.setOnHandQty(beforeOnHandQty + item.getQty());
                        balanceRepository.save(balance);

                        Map<String, Object> itemMetadata = new LinkedHashMap<>();
                        itemMetadata.put("variantId", item.getVariantId());
                        itemMetadata.put("qty", item.getQty());
                        itemMetadata.put("locationId", transfer.getSourceLocation().getId());
                        itemMetadata.put("beforeOnHandQty", beforeOnHandQty);
                        itemMetadata.put("afterOnHandQty", balance.getOnHandQty());
                        itemMetadata.put("reservedQty", balance.getReservedQty());
                        return itemMetadata;
                    })
                    .toList();
        }

        String fromStatus = transfer.getStatus();
        transfer.setStatus("CANCELLED");
        InventoryTransferEntity savedTransfer = transferRepository.save(transfer);
        auditTransferStatusChange(savedTransfer, fromStatus, "CANCELLED", Map.of("restoredItems", restoredItems));
    }

    public void receiveTransfer(Long transferId, ReceiveTransferRequest request) {
        receiveTransfer(transferId, request, LocationScope.all());
    }

    public void receiveTransfer(Long transferId, ReceiveTransferRequest request, LocationScope locationScope) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_002", "Transfer not found"));
        requireLocationScope(locationScope, transfer.getDestinationLocation().getId());

        if (!"IN_TRANSIT".equals(transfer.getStatus())) {
            throw new BusinessException("INV_002", "Transfer must be in IN_TRANSIT state to receive");
        }

        List<ReceiveTransferRequest.ReceivedItemDto> receivedItemsList = request.getReceivedItems();
        if (receivedItemsList == null || receivedItemsList.isEmpty()) {
            receivedItemsList = transfer.getItems().stream().map(item -> {
                ReceiveTransferRequest.ReceivedItemDto dto = new ReceiveTransferRequest.ReceivedItemDto();
                dto.setVariantId(item.getVariantId());
                dto.setReceivedQty(item.getQty());
                dto.setDamagedQty(0);
                return dto;
            }).collect(Collectors.toList());
        }

        Map<Long, ReceiveTransferRequest.ReceivedItemDto> receivedItemsByVariant = new LinkedHashMap<>();
        for (ReceiveTransferRequest.ReceivedItemDto receivedItem : receivedItemsList) {
            if (receivedItemsByVariant.putIfAbsent(receivedItem.getVariantId(), receivedItem) != null) {
                throw new BusinessException("INV_002", "Transfer receipt cannot contain duplicate variants");
            }
        }

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            if (!receivedItemsByVariant.containsKey(item.getVariantId())) {
                throw new BusinessException("INV_002", "All transfer items must be received before completion");
            }
        }
        if (receivedItemsByVariant.size() != transfer.getItems().size()) {
            throw new BusinessException("INV_002", "Received variants must match transfer items");
        }

        List<Map<String, Object>> auditedItems = transfer.getItems().stream()
                .map(item -> {
                    ReceiveTransferRequest.ReceivedItemDto receivedItem = receivedItemsByVariant.get(item.getVariantId());
                    int receivedQty = receivedItem.getReceivedQty();
                    int damagedQty = receivedItem.getDamagedQty();
                    if (receivedQty + damagedQty > item.getQty()) {
                        throw new BusinessException("INV_002", "Received plus damaged quantity cannot exceed transfer quantity");
                    }

                    item.setReceivedQty(receivedQty);
                    item.setDamagedQty(damagedQty);

                    InventoryBalanceEntity balance = balanceRepository
                            .findForUpdateByVariantIdAndLocationId(item.getVariantId(), transfer.getDestinationLocation().getId())
                            .orElseGet(() -> {
                                InventoryBalanceEntity newBalance = new InventoryBalanceEntity();
                                newBalance.setVariantId(item.getVariantId());
                                newBalance.setLocation(transfer.getDestinationLocation());
                                newBalance.setOnHandQty(0);
                                newBalance.setReservedQty(0);
                                return newBalance;
                            });

                    int beforeOnHandQty = balance.getOnHandQty();
                    balance.setOnHandQty(beforeOnHandQty + receivedQty);
                    balanceRepository.save(balance);

                    Map<String, Object> itemMetadata = new LinkedHashMap<>();
                    itemMetadata.put("variantId", item.getVariantId());
                    itemMetadata.put("transferQty", item.getQty());
                    itemMetadata.put("receivedQty", receivedQty);
                    itemMetadata.put("damagedQty", damagedQty);
                    itemMetadata.put("locationId", transfer.getDestinationLocation().getId());
                    itemMetadata.put("beforeOnHandQty", beforeOnHandQty);
                    itemMetadata.put("afterOnHandQty", balance.getOnHandQty());
                    itemMetadata.put("reservedQty", balance.getReservedQty());
                    return itemMetadata;
                })
                .toList();

        String fromStatus = transfer.getStatus();
        transfer.setStatus("COMPLETED");
        transfer.setCompletedAt(OffsetDateTime.now());
        InventoryTransferEntity savedTransfer = transferRepository.save(transfer);
        auditTransferStatusChange(savedTransfer, fromStatus, "COMPLETED", Map.of("items", auditedItems));
    }



    private void requireLocationScope(LocationScope locationScope, Long locationId) {
        if (!locationScope.allows(locationId)) {
            throw new ForbiddenOperationException("Missing location access: " + locationId);
        }
    }

    private void auditTransferStatusChange(
            InventoryTransferEntity transfer,
            String fromStatus,
            String toStatus,
            Map<String, ?> extraMetadata) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("transferId", transfer.getId());
        metadata.put("sourceLocationId", transfer.getSourceLocation().getId());
        metadata.put("destinationLocationId", transfer.getDestinationLocation().getId());
        metadata.put("fromStatus", fromStatus);
        metadata.put("toStatus", toStatus);
        metadata.put("itemCount", transfer.getItems().size());
        metadata.put("items", transfer.getItems().stream()
                .map(this::transferItemMetadata)
                .toList());
        metadata.putAll(extraMetadata);
        auditService.write(
                AuditAction.INVENTORY_TRANSFER_CHANGED,
                "InventoryTransfer",
                transfer.getId() == null ? null : transfer.getId().toString(),
                metadata);
    }

    private Map<String, Object> transferItemMetadata(InventoryTransferItemEntity item) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("variantId", item.getVariantId());
        metadata.put("qty", item.getQty());
        metadata.put("receivedQty", item.getReceivedQty());
        metadata.put("damagedQty", item.getDamagedQty());
        return metadata;
    }

    private InventoryBalanceDto mapBalance(InventoryBalanceEntity entity) {
        InventoryBalanceDto dto = new InventoryBalanceDto();
        dto.setId(entity.getId());
        dto.setLocationId(entity.getLocation().getId());
        dto.setLocationName(entity.getLocation().getDisplayName());
        dto.setVariantId(entity.getVariantId());
        dto.setOnHandQty(entity.getOnHandQty());
        dto.setReservedQty(entity.getReservedQty());
        dto.setAvailableQty(entity.getOnHandQty() - entity.getReservedQty());

        productVariantRepository.findById(entity.getVariantId()).ifPresent(variant -> {
            dto.setSkuCode(variant.getSkuCode());
            dto.setProductName(variant.getProduct().getName());
        });
        return dto;
    }

    private InventoryAdjustmentResponse mapAdjustment(InventoryBalanceEntity entity) {
        InventoryAdjustmentResponse response = new InventoryAdjustmentResponse();
        response.setInventoryBalanceId(entity.getId());
        response.setOnHandQty(entity.getOnHandQty());
        response.setReservedQty(entity.getReservedQty());
        response.setAvailableQty(entity.getOnHandQty() - entity.getReservedQty());
        return response;
    }

    private StockTransferDto mapTransfer(InventoryTransferEntity entity) {
        StockTransferDto dto = new StockTransferDto();
        dto.setId(entity.getId());
        dto.setSourceLocationName(entity.getSourceLocation().getDisplayName());
        dto.setDestinationLocationName(entity.getDestinationLocation().getDisplayName());
        dto.setStatus(entity.getStatus());
        dto.setItemCount(entity.getItems().size());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private ProductVariantDto mapVariant(ProductVariantEntity variant) {
        ProductVariantDto dto = new ProductVariantDto();
        dto.setId(variant.getId());
        dto.setSkuCode(variant.getSkuCode());
        dto.setProductName(variant.getProduct().getName());
        dto.setPrice(variant.getPrice());
        return dto;
    }
}
