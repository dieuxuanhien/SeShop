package com.seshop.inventory.api.dto;

public record LocationDto(
        Long id,
        String code,
        String displayName,
        String locationType,
        String status
) {
}
