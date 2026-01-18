package com.example.customersupport.dto;

public class OllamaRequestDTO {

    private String model;
    private String prompt;
    private boolean stream = false;

    public OllamaRequestDTO(String model, String prompt) {
        this.model = model;
        this.prompt = prompt;
    }

    public String getModel() {
        return model;
    }

    public String getPrompt() {
        return prompt;
    }

    public boolean isStream() {
        return stream;
    }
}
