package com.seshop.inventory.application;

import com.seshop.inventory.api.dto.CreateTransferRequest;
import com.seshop.inventory.api.dto.InventoryAdjustmentRequest;
import com.seshop.inventory.api.dto.LocationAvailabilityDto;
import com.seshop.inventory.api.dto.ReceiveTransferRequest;
import com.seshop.inventory.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryService {

    private final InventoryBalanceRepository balanceRepository;
    private final LocationRepository locationRepository;
    private final InventoryTransferRepository transferRepository;

    public InventoryService(InventoryBalanceRepository balanceRepository,
                            LocationRepository locationRepository,
                            InventoryTransferRepository transferRepository) {
        this.balanceRepository = balanceRepository;
        this.locationRepository = locationRepository;
        this.transferRepository = transferRepository;
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

    public void adjustInventory(InventoryAdjustmentRequest request) {
        LocationEntity location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        InventoryBalanceEntity balance = balanceRepository
                .findByVariantIdAndLocationId(request.getVariantId(), location.getId())
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
            throw new IllegalStateException("Adjustment would result in available quantity less than 0");
        }

        balanceRepository.save(balance);
    }

    public Long createTransfer(CreateTransferRequest request, Long createdBy) {
        LocationEntity source = locationRepository.findById(request.getSourceLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Source location not found"));
        LocationEntity destination = locationRepository.findById(request.getDestinationLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Destination location not found"));

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
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found"));

        if (!"DRAFT".equals(transfer.getStatus())) {
            throw new IllegalStateException("Transfer must be in DRAFT state to approve");
        }

        // Deduct from source location
        for (InventoryTransferItemEntity item : transfer.getItems()) {
            InventoryBalanceEntity balance = balanceRepository
                    .findByVariantIdAndLocationId(item.getVariantId(), transfer.getSourceLocation().getId())
                    .orElseThrow(() -> new IllegalStateException("Source balance not found for variant"));
            
            balance.setOnHandQty(balance.getOnHandQty() - item.getQty());
            if (balance.getOnHandQty() < balance.getReservedQty()) {
                throw new IllegalStateException("Insufficient available stock at source location");
            }
            balanceRepository.save(balance);
        }

        transfer.setStatus("IN_TRANSIT");
        transferRepository.save(transfer);
    }

    public void receiveTransfer(Long transferId, ReceiveTransferRequest request) {
        InventoryTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found"));

        if (!"IN_TRANSIT".equals(transfer.getStatus())) {
            throw new IllegalStateException("Transfer must be in IN_TRANSIT state to receive");
        }

        // Add to destination location and update items
        for (ReceiveTransferRequest.ReceivedItemDto receivedItem : request.getReceivedItems()) {
            InventoryTransferItemEntity item = transfer.getItems().stream()
                    .filter(i -> i.getVariantId().equals(receivedItem.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found in transfer"));

            item.setReceivedQty(receivedItem.getReceivedQty());
            item.setDamagedQty(receivedItem.getDamagedQty());

            InventoryBalanceEntity balance = balanceRepository
                    .findByVariantIdAndLocationId(item.getVariantId(), transfer.getDestinationLocation().getId())
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
}
