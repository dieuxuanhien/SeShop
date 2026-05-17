package com.seshop.marketing.application;

import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.marketing.api.dto.DiscountDto;
import com.seshop.marketing.api.dto.DiscountValidateRequest;
import com.seshop.marketing.api.dto.DiscountValidationResponse;
import com.seshop.marketing.infrastructure.persistence.DiscountCodeEntity;
import com.seshop.marketing.infrastructure.persistence.DiscountCodeRepository;
import com.seshop.marketing.infrastructure.persistence.DiscountRedemptionEntity;
import com.seshop.marketing.infrastructure.persistence.DiscountRedemptionRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.DuplicateResourceException;
import com.seshop.shared.exception.ResourceNotFoundException;
import com.seshop.shared.exception.SeShopValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String TYPE_PERCENT = "PERCENT";

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountRedemptionRepository redemptionRepository;
    private final AuditService auditService;

    public DiscountService(DiscountCodeRepository discountCodeRepository,
            DiscountRedemptionRepository redemptionRepository,
            AuditService auditService) {
        this.discountCodeRepository = discountCodeRepository;
        this.redemptionRepository = redemptionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public DiscountDto createDiscount(DiscountDto request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new SeShopValidationException("Discount code is required");
        }
        String code = request.getCode().trim();
        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new SeShopValidationException("Start and end time are required");
        }
        if (request.getEndAt().isBefore(request.getStartAt())) {
            throw new SeShopValidationException("End time must be after start time");
        }
        if (discountCodeRepository.findByCode(code).isPresent()) {
            throw new DuplicateResourceException("DISC_409", "Discount code already exists");
        }

        DiscountCodeEntity entity = new DiscountCodeEntity();
        entity.setCode(code);
        entity.setDiscountType(request.getDiscountType());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setMinSpend(request.getMinSpend() != null ? request.getMinSpend() : BigDecimal.ZERO);
        entity.setMaxUses(request.getMaxUses());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setStatus(STATUS_ACTIVE);

        DiscountCodeEntity saved = discountCodeRepository.save(entity);
        auditService.write(AuditAction.DISCOUNT_CREATED, "DiscountCode", targetId(saved), auditSnapshot(saved));
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DiscountDto> listDiscounts() {
        return discountCodeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscountDto updateDiscount(@NonNull Long id, DiscountDto request) {
        DiscountCodeEntity entity = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DISC_404", "Discount not found"));
        Map<String, Object> before = auditSnapshot(entity);

        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new SeShopValidationException("Start and end time are required");
        }
        if (request.getEndAt().isBefore(request.getStartAt())) {
            throw new SeShopValidationException("End time must be after start time");
        }

        entity.setDiscountType(request.getDiscountType());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setMinSpend(request.getMinSpend() != null ? request.getMinSpend() : BigDecimal.ZERO);
        entity.setMaxUses(request.getMaxUses());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());

        DiscountCodeEntity saved = discountCodeRepository.save(entity);
        auditService.write(AuditAction.DISCOUNT_UPDATED, "DiscountCode", targetId(saved),
                changeMetadata(before, auditSnapshot(saved)));
        return mapToDto(saved);
    }

    @Transactional
    public void deactivateDiscount(@NonNull Long id) {
        DiscountCodeEntity entity = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DISC_404", "Discount not found"));
        Map<String, Object> before = auditSnapshot(entity);
        entity.setStatus(STATUS_INACTIVE);
        DiscountCodeEntity saved = discountCodeRepository.save(entity);
        auditService.write(AuditAction.DISCOUNT_DEACTIVATED, "DiscountCode", targetId(saved),
                changeMetadata(before, auditSnapshot(saved)));
    }

    @Transactional(readOnly = true)
    public DiscountValidationResponse validateDiscount(DiscountValidateRequest request) {
        DiscountCodeEntity entity = findEligibleDiscount(
                request == null ? null : request.getCode(),
                request == null ? null : request.getOrderSubtotal());
        return validationResponse(entity, request == null ? null : request.getOrderSubtotal());
    }

    @Transactional
    public DiscountValidationResponse redeemDiscount(
            String code,
            Long customerId,
            Long orderId,
            BigDecimal orderSubtotal
    ) {
        if (customerId == null) {
            throw new SeShopValidationException("Customer is required");
        }
        if (orderId == null) {
            throw new SeShopValidationException("Order is required");
        }
        if (redemptionRepository.existsByOrderId(orderId)) {
            throw new BusinessException("DISC_006", "Order already has a discount redemption");
        }

        DiscountCodeEntity entity = findEligibleDiscount(code, orderSubtotal);
        DiscountValidationResponse response = validationResponse(entity, orderSubtotal);

        DiscountRedemptionEntity redemption = new DiscountRedemptionEntity();
        redemption.setDiscountCode(entity);
        redemption.setOrderId(orderId);
        redemption.setCustomerUserId(customerId);
        redemptionRepository.save(redemption);
        return response;
    }

    private DiscountCodeEntity findEligibleDiscount(String code, BigDecimal orderSubtotal) {
        if (code == null || code.isBlank()) {
            throw new SeShopValidationException("Discount code is required");
        }

        DiscountCodeEntity entity = discountCodeRepository.findByCode(code.trim())
                .orElseThrow(() -> new BusinessException("DISC_001", "Invalid discount code"));

        if (!STATUS_ACTIVE.equals(entity.getStatus())) {
            throw new BusinessException("DISC_002", "Discount code is not active");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (entity.getStartAt() != null && entity.getEndAt() != null
                && (now.isBefore(entity.getStartAt()) || now.isAfter(entity.getEndAt()))) {
            throw new BusinessException("DISC_003", "Discount code is expired or not yet started");
        }

        if (entity.getMinSpend() != null
                && nonNegativeSubtotal(orderSubtotal).compareTo(entity.getMinSpend()) < 0) {
            throw new BusinessException("DISC_004", "Minimum spend requirement not met");
        }

        if (entity.getMaxUses() != null) {
            long redemptionCount = redemptionRepository.countByDiscountCodeId(entity.getId());
            if (redemptionCount >= entity.getMaxUses()) {
                throw new BusinessException("DISC_005", "Discount code usage limit reached");
            }
        }

        return entity;
    }

    private DiscountValidationResponse validationResponse(DiscountCodeEntity entity, BigDecimal orderSubtotal) {
        DiscountValidationResponse response = new DiscountValidationResponse();
        response.setValid(true);
        response.setDiscountAmount(calculateDiscountAmount(entity, orderSubtotal));
        return response;
    }

    private BigDecimal calculateDiscountAmount(DiscountCodeEntity entity, BigDecimal subtotal) {
        BigDecimal base = nonNegativeSubtotal(subtotal);
        BigDecimal amount;
        if (TYPE_PERCENT.equalsIgnoreCase(entity.getDiscountType())) {
            amount = base.multiply(entity.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else {
            amount = entity.getDiscountValue();
        }
        if (amount == null || amount.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return amount.compareTo(base) > 0 ? base : amount;
    }

    private BigDecimal nonNegativeSubtotal(BigDecimal subtotal) {
        if (subtotal == null || subtotal.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return subtotal;
    }

    private Map<String, Object> changeMetadata(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("code", after.get("code"));
        metadata.put("before", before);
        metadata.put("after", after);
        return metadata;
    }

    private Map<String, Object> auditSnapshot(DiscountCodeEntity entity) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("code", entity.getCode());
        metadata.put("discountType", entity.getDiscountType());
        metadata.put("discountValue", entity.getDiscountValue());
        metadata.put("minSpend", entity.getMinSpend());
        metadata.put("maxUses", entity.getMaxUses());
        metadata.put("startAt", entity.getStartAt());
        metadata.put("endAt", entity.getEndAt());
        metadata.put("status", entity.getStatus());
        return metadata;
    }

    private String targetId(DiscountCodeEntity entity) {
        return entity.getId() == null ? null : entity.getId().toString();
    }

    private DiscountDto mapToDto(DiscountCodeEntity entity) {
        DiscountDto dto = new DiscountDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setDiscountType(entity.getDiscountType());
        dto.setDiscountValue(entity.getDiscountValue());
        dto.setMinSpend(entity.getMinSpend());
        dto.setMaxUses(entity.getMaxUses());
        dto.setStartAt(entity.getStartAt());
        dto.setEndAt(entity.getEndAt());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
