package com.seshop.inventory.application;

import com.seshop.inventory.api.dto.CreateCycleCountRequest;
import com.seshop.inventory.api.dto.CycleCountItemsRequest;
import com.seshop.inventory.infrastructure.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class CycleCountService {

    private final CycleCountRepository cycleCountRepository;
    private final LocationRepository locationRepository;
    private final InventoryBalanceRepository balanceRepository;

    public CycleCountService(CycleCountRepository cycleCountRepository,
                             LocationRepository locationRepository,
                             InventoryBalanceRepository balanceRepository) {
        this.cycleCountRepository = cycleCountRepository;
        this.locationRepository = locationRepository;
        this.balanceRepository = balanceRepository;
    }

    public Long createCycleCount(CreateCycleCountRequest request, Long startedBy) {
        LocationEntity location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        CycleCountEntity cycleCount = new CycleCountEntity();
        cycleCount.setLocation(location);
        cycleCount.setStatus("IN_PROGRESS");
        cycleCount.setStartedBy(startedBy);

        CycleCountEntity saved = cycleCountRepository.save(cycleCount);
        return saved.getId();
    }

    public void submitItems(Long cycleCountId, CycleCountItemsRequest request) {
        CycleCountEntity cycleCount = cycleCountRepository.findById(cycleCountId)
                .orElseThrow(() -> new IllegalArgumentException("Cycle count not found"));

        if (!"IN_PROGRESS".equals(cycleCount.getStatus())) {
            throw new IllegalStateException("Cycle count is not in progress");
        }

        for (CycleCountItemsRequest.CountedItemDto countedItem : request.getItems()) {
            CycleCountItemEntity item = cycleCount.getItems().stream()
                    .filter(i -> i.getVariantId().equals(countedItem.getVariantId()))
                    .findFirst()
                    .orElseGet(() -> {
                        CycleCountItemEntity newItem = new CycleCountItemEntity();
                        newItem.setCycleCount(cycleCount);
                        newItem.setVariantId(countedItem.getVariantId());
                        
                        // Get system quantity
                        InventoryBalanceEntity balance = balanceRepository
                                .findByVariantIdAndLocationId(countedItem.getVariantId(), cycleCount.getLocation().getId())
                                .orElse(null);
                        
                        newItem.setSystemQty(balance != null ? balance.getOnHandQty() : 0);
                        cycleCount.getItems().add(newItem);
                        return newItem;
                    });

            item.setCountedQty(countedItem.getCountedQty());
            item.setReasonCode(countedItem.getReasonCode());
        }

        cycleCountRepository.save(cycleCount);
    }

    public void approveCycleCount(Long cycleCountId, Long approvedBy) {
        CycleCountEntity cycleCount = cycleCountRepository.findById(cycleCountId)
                .orElseThrow(() -> new IllegalArgumentException("Cycle count not found"));

        if (!"IN_PROGRESS".equals(cycleCount.getStatus())) {
            throw new IllegalStateException("Cycle count is not in progress");
        }

        for (CycleCountItemEntity item : cycleCount.getItems()) {
            int variance = item.getCountedQty() - item.getSystemQty();
            
            if (variance != 0) {
                InventoryBalanceEntity balance = balanceRepository
                        .findByVariantIdAndLocationId(item.getVariantId(), cycleCount.getLocation().getId())
                        .orElseGet(() -> {
                            InventoryBalanceEntity newBalance = new InventoryBalanceEntity();
                            newBalance.setVariantId(item.getVariantId());
                            newBalance.setLocation(cycleCount.getLocation());
                            newBalance.setOnHandQty(0);
                            newBalance.setReservedQty(0);
                            return newBalance;
                        });
                        
                balance.setOnHandQty(item.getCountedQty());
                balanceRepository.save(balance);
            }
        }

        cycleCount.setStatus("APPROVED");
        cycleCount.setApprovedBy(approvedBy);
        cycleCount.setApprovedAt(OffsetDateTime.now());
        cycleCountRepository.save(cycleCount);
    }
}
