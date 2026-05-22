package com.seshop.pos.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.catalog.infrastructure.persistence.ProductVariantEntity;
import com.seshop.catalog.infrastructure.persistence.ProductVariantRepository;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceEntity;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.pos.api.dto.ProcessPosSaleRequest;
import com.seshop.pos.api.dto.ProcessPosSaleResponse;
import com.seshop.pos.api.dto.ReceiptDto;
import com.seshop.pos.infrastructure.persistence.PosReceiptEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptItemEntity;
import com.seshop.pos.infrastructure.persistence.PosReceiptRepository;
import com.seshop.pos.infrastructure.persistence.PosShiftEntity;
import com.seshop.pos.infrastructure.persistence.PosShiftRepository;
import com.seshop.shared.api.PageResponse;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ForbiddenOperationException;
import com.seshop.shared.exception.ResourceNotFoundException;
import com.seshop.shared.security.LocationScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.seshop.inventory.infrastructure.persistence.LocationEntity;
import com.seshop.inventory.infrastructure.persistence.LocationRepository;
import com.seshop.identity.infrastructure.persistence.UserEntity;
import com.seshop.identity.infrastructure.persistence.UserRepository;

@Service
@Transactional
public class ReceiptService {

    private final PosReceiptRepository receiptRepository;
    private final PosShiftRepository shiftRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final AuditService auditService;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public ReceiptService(PosReceiptRepository receiptRepository,
                          PosShiftRepository shiftRepository,
                          ProductVariantRepository productVariantRepository,
                          InventoryBalanceRepository balanceRepository,
                          AuditService auditService,
                          LocationRepository locationRepository,
                          UserRepository userRepository) {
        this.receiptRepository = receiptRepository;
        this.shiftRepository = shiftRepository;
        this.productVariantRepository = productVariantRepository;
        this.balanceRepository = balanceRepository;
        this.auditService = auditService;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(String receiptNumber) {
        return getReceipt(receiptNumber, LocationScope.all());
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(String receiptNumber, LocationScope locationScope) {
        return getReceipt(parseReceiptId(receiptNumber), locationScope);
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(Long receiptId) {
        return getReceipt(receiptId, LocationScope.all());
    }

    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(Long receiptId, LocationScope locationScope) {
        PosReceiptEntity receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("POS_404", "Receipt not found"));
        requireLocationScope(locationScope, receipt.getShift().getLocationId());
        return mapToDto(receipt);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReceiptDto> listReceipts(int page, int size) {
        return listReceipts(page, size, LocationScope.all());
    }

    @Transactional(readOnly = true)
    public PageResponse<ReceiptDto> listReceipts(int page, int size, LocationScope locationScope) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<PosReceiptEntity> receipts = locationScope.isEmpty()
                ? new PageImpl<>(List.of(), pageRequest, 0)
                : locationScope.allLocations()
                        ? receiptRepository.findAll(pageRequest)
                        : receiptRepository.findByShiftLocationIds(locationScope.locationIds(), pageRequest);
        return new PageResponse<>(
                receipts.getContent().stream().map(this::mapToDto).toList(),
                receipts.getNumber(),
                receipts.getSize(),
                receipts.getTotalElements(),
                receipts.getTotalPages());
    }

    public ProcessPosSaleResponse createReceipt(ProcessPosSaleRequest request, Long staffId) {
        return createReceipt(request, staffId, LocationScope.all());
    }

    public ProcessPosSaleResponse createReceipt(ProcessPosSaleRequest request, Long staffId, LocationScope locationScope) {
        String paymentMethod = normalizePaymentMethod(request.getPaymentMethod());
        PosShiftEntity shift = request.getShiftId() != null
                ? shiftRepository.findById(request.getShiftId())
                        .orElseThrow(() -> new ResourceNotFoundException("POS_002", "Shift not found"))
                : shiftRepository.findByStaffIdAndStatus(staffId, "OPEN")
                        .orElseThrow(() -> new BusinessException("POS_002", "No active shift found"));

        if (!"OPEN".equals(shift.getStatus())) {
            throw new BusinessException("POS_002", "Active shift required");
        }
        if (!shift.getStaffId().equals(staffId)) {
            throw new ForbiddenOperationException("Shift belongs to another staff member");
        }
        requireLocationScope(locationScope, shift.getLocationId());

        PosReceiptEntity receipt = new PosReceiptEntity();
        receipt.setShift(shift);
        receipt.setCustomerUserId(request.getCustomerUserId());
        receipt.setPaymentMethod(paymentMethod);

        BigDecimal total = BigDecimal.ZERO;
        List<Map<String, Object>> auditedItems = new ArrayList<>();
        for (ProcessPosSaleRequest.Item item : request.getItems()) {
            ProductVariantEntity variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("CAT_404", "Variant not found"));
            BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
            total = total.add(lineTotal);

            InventoryBalanceEntity balance = balanceRepository
                    .findForUpdateByVariantIdAndLocationId(item.getVariantId(), shift.getLocationId())
                    .orElseThrow(() -> new BusinessException("INV_001", "Insufficient stock at POS location"));
            int availableQty = balance.getOnHandQty() - balance.getReservedQty();
            if (availableQty < item.getQty()) {
                throw new BusinessException("INV_001", "Insufficient stock at POS location");
            }
            int beforeOnHandQty = balance.getOnHandQty();
            balance.setOnHandQty(balance.getOnHandQty() - item.getQty());
            balanceRepository.save(balance);
            auditedItems.add(Map.of(
                    "variantId", item.getVariantId(),
                    "qty", item.getQty(),
                    "unitPrice", variant.getPrice(),
                    "locationId", shift.getLocationId(),
                    "beforeOnHandQty", beforeOnHandQty,
                    "afterOnHandQty", balance.getOnHandQty(),
                    "reservedQty", balance.getReservedQty()
            ));

            PosReceiptItemEntity receiptItem = new PosReceiptItemEntity();
            receiptItem.setReceipt(receipt);
            receiptItem.setVariantId(item.getVariantId());
            receiptItem.setQty(item.getQty());
            receiptItem.setUnitPrice(variant.getPrice());
            receipt.getItems().add(receiptItem);
        }

        if ("CASH".equals(paymentMethod) && request.getAmountPaid().compareTo(total) < 0) {
            throw new BusinessException("POS_003", "Amount paid is less than receipt total");
        }

        receipt.setTotalAmount(total);
        PosReceiptEntity savedReceipt = receiptRepository.save(receipt);
        String receiptNumber = formatReceiptNumber(savedReceipt.getId());

        ProcessPosSaleResponse response = new ProcessPosSaleResponse();
        response.setReceiptId(savedReceipt.getId());
        response.setReceiptNumber(receiptNumber);
        BigDecimal changeDue = "CASH".equals(paymentMethod)
                ? request.getAmountPaid().subtract(total)
                : BigDecimal.ZERO;
        response.setChangeDue(changeDue);
        response.setTotalAmount(total);
        response.setPaymentMethod(paymentMethod);
        response.setAmountPaid(request.getAmountPaid());
        response.setCreatedAt(savedReceipt.getCreatedAt());

        LocationEntity location = locationRepository.findById(shift.getLocationId()).orElse(null);
        response.setLocationName(location != null ? location.getDisplayName() : "Unknown Store");

        UserEntity staffUser = userRepository.findById(staffId).orElse(null);
        response.setOperatorName(staffUser != null ? staffUser.getUsername() : "Staff");

        List<ProcessPosSaleResponse.ItemResponse> itemResponses = new ArrayList<>();
        for (PosReceiptItemEntity savedItem : savedReceipt.getItems()) {
            ProductVariantEntity variant = productVariantRepository.findById(savedItem.getVariantId()).orElse(null);
            
            ProcessPosSaleResponse.ItemResponse itemResp = new ProcessPosSaleResponse.ItemResponse();
            itemResp.setId(savedItem.getId());
            itemResp.setVariantId(savedItem.getVariantId());
            itemResp.setSkuCode(variant != null ? variant.getSkuCode() : "N/A");
            itemResp.setName(variant != null && variant.getProduct() != null ? variant.getProduct().getName() : "Unknown Variant");
            itemResp.setQty(savedItem.getQty());
            itemResp.setUnitPrice(savedItem.getUnitPrice());
            itemResp.setTotalPrice(savedItem.getUnitPrice().multiply(BigDecimal.valueOf(savedItem.getQty())));
            itemResponses.add(itemResp);
        }
        response.setItems(itemResponses);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("receiptNumber", receiptNumber);
        metadata.put("staffId", staffId);
        metadata.put("customerUserId", request.getCustomerUserId());
        metadata.put("shiftId", shift.getId());
        metadata.put("locationId", shift.getLocationId());
        metadata.put("paymentMethod", paymentMethod);
        metadata.put("totalAmount", total);
        metadata.put("amountPaid", request.getAmountPaid());
        metadata.put("changeDue", response.getChangeDue());
        metadata.put("items", auditedItems);
        auditService.write(AuditAction.POS_SALE_COMPLETED, "PosReceipt", savedReceipt.getId().toString(), metadata);
        return response;
    }

    private void requireLocationScope(LocationScope locationScope, Long locationId) {
        if (!locationScope.allows(locationId)) {
            throw new ForbiddenOperationException("Missing location access: " + locationId);
        }
    }

    private ReceiptDto mapToDto(PosReceiptEntity entity) {
        ReceiptDto dto = new ReceiptDto();
        dto.setId(entity.getId());
        dto.setReceiptNumber(formatReceiptNumber(entity.getId()));
        dto.setReceiptContent("Receipt total: " + entity.getTotalAmount() + " (" + entity.getPaymentMethod() + ")");
        dto.setIssuedAt(entity.getCreatedAt());
        
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setAmountPaid(entity.getTotalAmount());

        if (entity.getShift() != null) {
            LocationEntity location = locationRepository.findById(entity.getShift().getLocationId()).orElse(null);
            dto.setLocationName(location != null ? location.getDisplayName() : "Unknown Store");

            UserEntity staffUser = userRepository.findById(entity.getShift().getStaffId()).orElse(null);
            dto.setOperatorName(staffUser != null ? staffUser.getUsername() : "Staff");
        } else {
            dto.setLocationName("Unknown Store");
            dto.setOperatorName("Staff");
        }

        List<ReceiptDto.ItemDto> itemsList = new ArrayList<>();
        if (entity.getItems() != null) {
            for (PosReceiptItemEntity itemEntity : entity.getItems()) {
                ProductVariantEntity variant = productVariantRepository.findById(itemEntity.getVariantId()).orElse(null);
                
                ReceiptDto.ItemDto itemDto = new ReceiptDto.ItemDto();
                itemDto.setId(itemEntity.getId());
                itemDto.setVariantId(itemEntity.getVariantId());
                itemDto.setSkuCode(variant != null ? variant.getSkuCode() : "N/A");
                itemDto.setName(variant != null && variant.getProduct() != null ? variant.getProduct().getName() : "Unknown Variant");
                itemDto.setQty(itemEntity.getQty());
                itemDto.setUnitPrice(itemEntity.getUnitPrice());
                itemDto.setTotalPrice(itemEntity.getUnitPrice().multiply(BigDecimal.valueOf(itemEntity.getQty())));
                itemsList.add(itemDto);
            }
        }
        dto.setItems(itemsList);
        return dto;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String normalized = paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!"CASH".equals(normalized) && !"CARD".equals(normalized)) {
            throw new BusinessException("PAY_002", "Unsupported payment method");
        }
        return normalized;
    }

    private Long parseReceiptId(String receiptNumber) {
        if (receiptNumber == null || receiptNumber.isBlank()) {
            throw new ResourceNotFoundException("POS_404", "Receipt not found");
        }
        String normalized = receiptNumber.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("POS-")) {
            normalized = normalized.substring(4);
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException exception) {
            throw new ResourceNotFoundException("POS_404", "Receipt not found");
        }
    }

    private String formatReceiptNumber(Long receiptId) {
        return String.format(Locale.ROOT, "POS-%08d", receiptId);
    }
}
