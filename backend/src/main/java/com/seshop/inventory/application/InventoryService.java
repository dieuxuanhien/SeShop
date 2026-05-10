package com.seshop.inventory.application;

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
import com.seshop.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

    public InventoryService(InventoryBalanceRepository balanceRepository,
                            LocationRepository locationRepository,
                            InventoryTransferRepository transferRepository,
                            ProductVariantRepository productVariantRepository) {
        this.balanceRepository = balanceRepository;
        this.locationRepository = locationRepository;
        this.transferRepository = transferRepository;
        this.productVariantRepository = productVariantRepository;
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
        Long resolvedVariantId = variantId;
        if (skuCode != null && !skuCode.isBlank()) {
            resolvedVariantId = productVariantRepository.findBySkuCode(skuCode)
                    .map(ProductVariantEntity::getId)
                    .orElse(-1L);
        }

        Page<InventoryBalanceEntity> balances = balanceRepository.search(
                resolvedVariantId,
                locationId,
                PageRequest.of(page, size));

        return new PageResponse<>(
                balances.getContent().stream().map(this::mapBalance).toList(),
                balances.getNumber(),
                balances.getSize(),
                balances.getTotalElements(),
                balances.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<StockTransferDto> listTransfers(int page, int size) {
        Page<InventoryTransferEntity> transfers = transferRepository.findAll(PageRequest.of(page, size));
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

    public InventoryAdjustmentResponse adjustInventory(InventoryAdjustmentRequest request) {
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

        balance.setOnHandQty(balance.getOnHandQty() + request.getDeltaQty());
        
        if (balance.getOnHandQty() < balance.getReservedQty()) {
            throw new BusinessException("INV_001", "Adjustment would result in available quantity less than 0");
        }

        return mapAdjustment(balanceRepository.save(balance));
    }

    public Long createTransfer(CreateTransferRequest request, Long createdBy) {
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
            InventoryTransferItemEntity item = new InventoryTransferItemEntity();
            item.setTransfer(transfer);
            item.setVariantId(itemDto.getVariantId());
            item.setQty(itemDto.getQty());
            transfer.getItems().add(item);
        }

        InventoryTransferEntity saved = transferRepository.save(transfer);
        return saved.getId();
    }

    public void approveTransfer(Long transferId) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_002", "Transfer not found"));

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new BusinessException("INV_002", "Transfer must be in DRAFT state to approve");
        }

        for (InventoryTransferItemEntity item : transfer.getItems()) {
            InventoryBalanceEntity balance = balanceRepository
                    .findForUpdateByVariantIdAndLocationId(item.getVariantId(), transfer.getSourceLocation().getId())
                    .orElseThrow(() -> new BusinessException("INV_001", "Source balance not found for variant"));
            
            balance.setOnHandQty(balance.getOnHandQty() - item.getQty());
            if (balance.getOnHandQty() < balance.getReservedQty()) {
                throw new BusinessException("INV_001", "Insufficient available stock at source location");
            }
            balanceRepository.save(balance);
        }

        transfer.setStatus("IN_TRANSIT");
        transferRepository.save(transfer);
    }

    public void receiveTransfer(Long transferId, ReceiveTransferRequest request) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("INV_002", "Transfer not found"));

        if (!"IN_TRANSIT".equals(transfer.getStatus())) {
            throw new BusinessException("INV_002", "Transfer must be in IN_TRANSIT state to receive");
        }

        for (ReceiveTransferRequest.ReceivedItemDto receivedItem : request.getReceivedItems()) {
            InventoryTransferItemEntity item = transfer.getItems().stream()
                    .filter(i -> i.getVariantId().equals(receivedItem.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("INV_002", "Variant not found in transfer"));

            item.setReceivedQty(receivedItem.getReceivedQty());
            item.setDamagedQty(receivedItem.getDamagedQty());

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

            balance.setOnHandQty(balance.getOnHandQty() + receivedItem.getReceivedQty());
            balanceRepository.save(balance);
        }

        transfer.setStatus("COMPLETED");
        transfer.setCompletedAt(OffsetDateTime.now());
        transferRepository.save(transfer);
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
