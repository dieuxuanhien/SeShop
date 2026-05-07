package com.seshop.marketing.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class InstagramDraftDto {
    private Long id;
    private Long productId;
    private String caption;
    private String hashtags;
    private List<String> mediaOrder;
    private String status;
    private OffsetDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getHashtags() { return hashtags; }
    public void setHashtags(String hashtags) { this.hashtags = hashtags; }
    public List<String> getMediaOrder() { return mediaOrder; }
    public void setMediaOrder(List<String> mediaOrder) { this.mediaOrder = mediaOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
