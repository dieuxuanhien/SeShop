package com.seshop.marketing.api.dto;

import java.time.OffsetDateTime;

public class InstagramPublishResultDto {
    private Long draftId;
    private String status;
    private String instagramCreationId;
    private String instagramMediaId;
    private String instagramPermalink;
    private OffsetDateTime publishedAt;

    public Long getDraftId() { return draftId; }
    public void setDraftId(Long draftId) { this.draftId = draftId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInstagramCreationId() { return instagramCreationId; }
    public void setInstagramCreationId(String instagramCreationId) { this.instagramCreationId = instagramCreationId; }
    public String getInstagramMediaId() { return instagramMediaId; }
    public void setInstagramMediaId(String instagramMediaId) { this.instagramMediaId = instagramMediaId; }
    public String getInstagramPermalink() { return instagramPermalink; }
    public void setInstagramPermalink(String instagramPermalink) { this.instagramPermalink = instagramPermalink; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(OffsetDateTime publishedAt) { this.publishedAt = publishedAt; }
}