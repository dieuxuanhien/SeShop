package com.seshop.catalog.api.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterProductImageRequest {
    @NotBlank(message = "Image URL is required")
    private String url;
    private Integer sortOrder;
    private Boolean isInstagramReady;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsInstagramReady() {
        return isInstagramReady;
    }

    public void setIsInstagramReady(Boolean isInstagramReady) {
        this.isInstagramReady = isInstagramReady;
    }
}
