package com.seshop.marketing.api.dto;

import java.util.Map;

public class AiRecommendationRequest {
    private String message;
    private Map<String, String> context;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, String> getContext() { return context; }
    public void setContext(Map<String, String> context) { this.context = context; }
}
