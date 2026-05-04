package com.seshop.commerce.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProcessOrderRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(CONFIRM|SHIP|DELIVER|CANCEL)$", message = "Invalid action")
    private String action;

    private String notes;

    // Getters and Setters
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
