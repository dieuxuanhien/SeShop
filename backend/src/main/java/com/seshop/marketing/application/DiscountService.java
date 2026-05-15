package com.seshop.marketing.application;

import com.seshop.marketing.api.dto.DiscountDto;
import com.seshop.marketing.api.dto.DiscountValidateRequest;
import com.seshop.marketing.api.dto.DiscountValidationResponse;
import com.seshop.marketing.infrastructure.persistence.DiscountCodeEntity;
import com.seshop.marketing.infrastructure.persistence.DiscountCodeRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepository;
    private final DiscountRedemptionRepository redemptionRepository;

    public DiscountService(DiscountCodeRepository discountCodeRepository,
            DiscountRedemptionRepository redemptionRepository) {
        this.discountCodeRepository = discountCodeRepository;
        this.redemptionRepository = redemptionRepository;
    }

    @Transactional
    public DiscountDto createDiscount(DiscountDto request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new SeShopValidationException("Discount code is required");
        }
        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new SeShopValidationException("Start and end time are required");
        }
        if (request.getEndAt().isBefore(request.getStartAt())) {
            throw new SeShopValidationException("End time must be after start time");
        }
        if (discountCodeRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("DISC_409", "Discount code already exists");
        }

        DiscountCodeEntity entity = new DiscountCodeEntity();
        entity.setCode(request.getCode());
        entity.setDiscountType(request.getDiscountType());
        entity.setDiscountValue(request.getDiscountValue());
        entity.setMinSpend(request.getMinSpend() != null ? request.getMinSpend() : BigDecimal.ZERO);
        entity.setMaxUses(request.getMaxUses());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setStatus("ACTIVE");

        DiscountCodeEntity saved = discountCodeRepository.save(entity);
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

        return mapToDto(discountCodeRepository.save(entity));
    }

    @Transactional
    public void deactivateDiscount(@NonNull Long id) {
        DiscountCodeEntity entity = discountCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DISC_404", "Discount not found"));
        entity.setStatus("INACTIVE");
        discountCodeRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public DiscountValidationResponse validateDiscount(DiscountValidateRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new SeShopValidationException("Discount code is required");
        }

        DiscountCodeEntity entity = discountCodeRepository.findByCode(request.getCode())
                .orElseThrow(() -> new BusinessException("DISC_001", "Invalid discount code"));

        if (!"ACTIVE".equals(entity.getStatus())) {
            throw new BusinessException("DISC_002", "Discount code is not active");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (entity.getStartAt() != null && entity.getEndAt() != null
                && (now.isBefore(entity.getStartAt()) || now.isAfter(entity.getEndAt()))) {
            throw new BusinessException("DISC_003", "Discount code is expired or not yet started");
        }

        if (request.getOrderSubtotal() != null && entity.getMinSpend() != null
                && request.getOrderSubtotal().compareTo(entity.getMinSpend()) < 0) {
            throw new BusinessException("DISC_004", "Minimum spend requirement not met");
        }

        if (entity.getMaxUses() != null) {
            long redemptionCount = redemptionRepository.countByDiscountCodeId(entity.getId());
            if (redemptionCount >= entity.getMaxUses()) {
                throw new BusinessException("DISC_005", "Discount code usage limit reached");
            }
        }

        DiscountValidationResponse response = new DiscountValidationResponse();
        response.setValid(true);
        response.setDiscountAmount(calculateDiscountAmount(entity, request.getOrderSubtotal()));
        return response;
    }

    private BigDecimal calculateDiscountAmount(DiscountCodeEntity entity, BigDecimal subtotal) {
        BigDecimal base = subtotal == null ? BigDecimal.ZERO : subtotal;
        if ("PERCENT".equalsIgnoreCase(entity.getDiscountType())) {
            return base.multiply(entity.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }
        return entity.getDiscountValue();
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
