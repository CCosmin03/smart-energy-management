package com.example.customersupport.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequestDTO {

    @NotBlank
    private String userId;

    @NotBlank
    private String message;

    // OPTIONAL: daca e null, folosim toggle-ul global din env
    private Boolean useRules;

    public ChatRequestDTO() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getUseRules() {
        return useRules;
    }

    public void setUseRules(Boolean useRules) {
        this.useRules = useRules;
    }
}
