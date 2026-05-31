package com.seshop.marketing.api.dto;

import java.util.List;
import java.util.Map;

public class AiRecommendationRequest {
    private String message;
    private Map<String, String> context;
    private Long customerId;
    private List<ConversationTurn> conversationHistory;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, String> getContext() { return context; }
    public void setContext(Map<String, String> context) { this.context = context; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public List<ConversationTurn> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(List<ConversationTurn> conversationHistory) { this.conversationHistory = conversationHistory; }

    public static class ConversationTurn {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}

