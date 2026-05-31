package com.seshop.inventory.application;

import com.seshop.inventory.api.dto.CreateCycleCountRequest;
import com.seshop.inventory.api.dto.CycleCountItemsRequest;
import com.seshop.inventory.infrastructure.persistence.*;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.LocationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional
public class CycleCountService {

    private final CycleCountRepository cycleCountRepository;
    private final LocationRepository locationRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final LocationAccessService locationAccessService;

    public CycleCountService(CycleCountRepository cycleCountRepository,
                             LocationRepository locationRepository,
                             InventoryBalanceRepository balanceRepository,
                             LocationAccessService locationAccessService) {
        this.cycleCountRepository = cycleCountRepository;
        this.locationRepository = locationRepository;
        this.balanceRepository = balanceRepository;
        this.locationAccessService = locationAccessService;
    }

    public Long createCycleCount(CreateCycleCountRequest request, AuthenticatedUser user) {
        locationAccessService.requireLocationAccess(user, request.getLocationId());
        LocationEntity location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        CycleCountEntity cycleCount = new CycleCountEntity();
        cycleCount.setLocation(location);
        cycleCount.setStatus("IN_PROGRESS");
        cycleCount.setStartedBy(user.userId());

        CycleCountEntity saved = cycleCountRepository.save(cycleCount);
        return saved.getId();
    }

    public void submitItems(Long cycleCountId, CycleCountItemsRequest request, AuthenticatedUser user) {
        CycleCountEntity cycleCount = cycleCountRepository.findById(cycleCountId)
                .orElseThrow(() -> new IllegalArgumentException("Cycle count not found"));
        locationAccessService.requireLocationAccess(user, cycleCount.getLocation().getId());

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

    public void approveCycleCount(Long cycleCountId, AuthenticatedUser user) {
        CycleCountEntity cycleCount = cycleCountRepository.findById(cycleCountId)
                .orElseThrow(() -> new IllegalArgumentException("Cycle count not found"));
        locationAccessService.requireLocationAccess(user, cycleCount.getLocation().getId());

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
        cycleCount.setApprovedBy(user.userId());
        cycleCount.setApprovedAt(OffsetDateTime.now());
        cycleCountRepository.save(cycleCount);
    }
}
