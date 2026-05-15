package com.seshop.marketing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.seshop.marketing.api.dto.DiscountDto;
import com.seshop.marketing.api.dto.DiscountValidateRequest;
import com.seshop.marketing.api.dto.DiscountValidationResponse;
import com.seshop.marketing.infrastructure.persistence.DiscountCodeEntity;
import com.seshop.marketing.infrastructure.persistence.DiscountCodeRepository;
import com.seshop.marketing.infrastructure.persistence.DiscountRedemptionRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.DuplicateResourceException;
import com.seshop.shared.exception.SeShopValidationException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UC10: Manage discount codes.
 * Covers BR28 (uniqueness), BR29 (single-code), BR30 (constraints: period, cap, min-spend).
 */
@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private DiscountCodeRepository discountCodeRepository;

    @Mock
    private DiscountRedemptionRepository redemptionRepository;

    private DiscountService service;

    @BeforeEach
    void setUp() {
        service = new DiscountService(discountCodeRepository, redemptionRepository);
    }

    // ── createDiscount ──────────────────────────────────────────────────────

    @Test
    void createDiscountPersistsActiveEntityAndReturnsDto() {
        given(discountCodeRepository.findByCode("SUMMER20")).willReturn(Optional.empty());
        given(discountCodeRepository.save(any())).willAnswer(inv -> {
            DiscountCodeEntity e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        DiscountDto request = discountRequest("SUMMER20", "PERCENT", "20", "500000", 100);

        DiscountDto result = service.createDiscount(request);

        ArgumentCaptor<DiscountCodeEntity> captor = ArgumentCaptor.forClass(DiscountCodeEntity.class);
        then(discountCodeRepository).should().save(captor.capture());
        DiscountCodeEntity saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("SUMMER20");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("SUMMER20");
    }

    @Test
    void createDiscountRejectsDuplicateCode() {
        given(discountCodeRepository.findByCode("DUP")).willReturn(Optional.of(new DiscountCodeEntity()));

        DiscountDto request = discountRequest("DUP", "PERCENT", "10", "0", null);

        assertThatThrownBy(() -> service.createDiscount(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createDiscountRejectsEndBeforeStart() {
        DiscountDto request = discountRequest("BAD", "FIXED", "50000", "0", null);
        request.setStartAt(OffsetDateTime.now().plusDays(5));
        request.setEndAt(OffsetDateTime.now().plusDays(1)); // earlier than start

        assertThatThrownBy(() -> service.createDiscount(request))
                .isInstanceOf(SeShopValidationException.class);
    }

    // ── deactivateDiscount ──────────────────────────────────────────────────

    @Test
    void deactivateDiscountSetsStatusToInactive() {
        DiscountCodeEntity entity = activeEntity("VINTAGE10", "PERCENT", "10");
        given(discountCodeRepository.findById(7L)).willReturn(Optional.of(entity));
        given(discountCodeRepository.save(entity)).willReturn(entity);

        service.deactivateDiscount(7L);

        assertThat(entity.getStatus()).isEqualTo("INACTIVE");
    }

    // ── validateDiscount ────────────────────────────────────────────────────

    @Test
    void validateDiscountHappyPathReturnsAmountForPercentType() {
        DiscountCodeEntity entity = activeEntity("VINTAGE10", "PERCENT", "10");
        given(discountCodeRepository.findByCode("VINTAGE10")).willReturn(Optional.of(entity));

        DiscountValidateRequest request = new DiscountValidateRequest();
        request.setCode("VINTAGE10");
        request.setOrderSubtotal(new BigDecimal("1000000"));

        DiscountValidationResponse response = service.validateDiscount(request);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("100000.00");
    }

    @Test
    void validateDiscountHappyPathReturnsFixedAmount() {
        DiscountCodeEntity entity = activeEntity("THRIFTED50K", "FIXED", "50000");
        given(discountCodeRepository.findByCode("THRIFTED50K")).willReturn(Optional.of(entity));

        DiscountValidateRequest request = new DiscountValidateRequest();
        request.setCode("THRIFTED50K");
        request.setOrderSubtotal(new BigDecimal("900000"));

        DiscountValidationResponse response = service.validateDiscount(request);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualByComparingTo("50000");
    }

    @Test
    void validateDiscountRejectsExpiredCode() {
        DiscountCodeEntity entity = activeEntity("OLD20", "PERCENT", "20");
        entity.setStartAt(OffsetDateTime.now().minusDays(60));
        entity.setEndAt(OffsetDateTime.now().minusDays(1));   // expired yesterday
        given(discountCodeRepository.findByCode("OLD20")).willReturn(Optional.of(entity));

        DiscountValidateRequest request = new DiscountValidateRequest();
        request.setCode("OLD20");
        request.setOrderSubtotal(new BigDecimal("500000"));

        assertThatThrownBy(() -> service.validateDiscount(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void validateDiscountRejectsWhenUsageCapReached() {
        DiscountCodeEntity entity = activeEntity("LIMITED", "PERCENT", "15");
        entity.setMaxUses(10);
        given(discountCodeRepository.findByCode("LIMITED")).willReturn(Optional.of(entity));
        given(redemptionRepository.countByDiscountCodeId(null)).willReturn(10L);

        DiscountValidateRequest request = new DiscountValidateRequest();
        request.setCode("LIMITED");
        request.setOrderSubtotal(new BigDecimal("800000"));

        assertThatThrownBy(() -> service.validateDiscount(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("usage limit");
    }

    @Test
    void validateDiscountRejectsWhenMinSpendNotMet() {
        DiscountCodeEntity entity = activeEntity("HIGHMIN", "FIXED", "100000");
        entity.setMinSpend(new BigDecimal("2000000"));
        given(discountCodeRepository.findByCode("HIGHMIN")).willReturn(Optional.of(entity));

        DiscountValidateRequest request = new DiscountValidateRequest();
        request.setCode("HIGHMIN");
        request.setOrderSubtotal(new BigDecimal("500000")); // below min spend

        assertThatThrownBy(() -> service.validateDiscount(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Minimum spend");
    }

    @Test
    void validateDiscountRejectsInactiveCode() {
        DiscountCodeEntity entity = activeEntity("INACTIVE", "PERCENT", "5");
        entity.setStatus("INACTIVE");
        given(discountCodeRepository.findByCode("INACTIVE")).willReturn(Optional.of(entity));

        DiscountValidateRequest request = new DiscountValidateRequest();
        request.setCode("INACTIVE");

        assertThatThrownBy(() -> service.validateDiscount(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not active");
    }

    // ── listDiscounts ───────────────────────────────────────────────────────

    @Test
    void listDiscountsReturnsMappedDtos() {
        DiscountCodeEntity e1 = activeEntity("CODE1", "PERCENT", "10");
        e1.setId(1L);
        DiscountCodeEntity e2 = activeEntity("CODE2", "FIXED", "20000");
        e2.setId(2L);
        given(discountCodeRepository.findAll()).willReturn(List.of(e1, e2));

        List<DiscountDto> results = service.listDiscounts();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getCode()).isEqualTo("CODE1");
        assertThat(results.get(1).getCode()).isEqualTo("CODE2");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private DiscountDto discountRequest(String code, String type, String value, String minSpend, Integer maxUses) {
        DiscountDto dto = new DiscountDto();
        dto.setCode(code);
        dto.setDiscountType(type);
        dto.setDiscountValue(new BigDecimal(value));
        dto.setMinSpend(new BigDecimal(minSpend));
        dto.setMaxUses(maxUses);
        dto.setStartAt(OffsetDateTime.now().minusDays(1));
        dto.setEndAt(OffsetDateTime.now().plusDays(30));
        return dto;
    }

    private DiscountCodeEntity activeEntity(String code, String type, String value) {
        DiscountCodeEntity entity = new DiscountCodeEntity();
        entity.setCode(code);
        entity.setDiscountType(type);
        entity.setDiscountValue(new BigDecimal(value));
        entity.setMinSpend(BigDecimal.ZERO);
        entity.setStatus("ACTIVE");
        entity.setStartAt(OffsetDateTime.now().minusDays(1));
        entity.setEndAt(OffsetDateTime.now().plusDays(30));
        return entity;
    }
}
