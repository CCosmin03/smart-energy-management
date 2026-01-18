package com.example.customersupport.service;

import com.example.customersupport.dto.OllamaRequestDTO;
import com.example.customersupport.dto.OllamaResponseDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Service
public class AiChatbotService {

    public static final String FALLBACK =
            "Momentan AI nu este disponibil. Te rog incearca din nou putin mai tarziu.";

    private final WebClient webClient;
    private final String model;
    private final long timeoutSeconds;

    public AiChatbotService(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model,
            @Value("${ollama.timeout-seconds}") long timeoutSeconds
    ) {
        this.model = model;
        this.timeoutSeconds = timeoutSeconds;

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(timeoutSeconds));

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        System.out.println("[AiChatbotService] baseUrl=" + baseUrl
                + " model=" + model
                + " timeoutSeconds=" + timeoutSeconds);
    }

    @PostConstruct
    public void warmUp() {
        // incarca modelul la startup ca sa nu moara primul mesaj
        try {
            String r = generateResponseInternal("Say hi in one short sentence.");
            System.out.println("[AiChatbotService] warmup=" + (r == null ? "null" : "ok"));
        } catch (Exception e) {
            System.out.println("[AiChatbotService] warmup failed: " + e.getMessage());
        }
    }

    public String generateResponse(String userMessage) {
        try {
            String r = generateResponseInternal(userMessage);
            if (r == null || r.isBlank()) return FALLBACK;
            return r.trim();
        } catch (Exception e) {
            return FALLBACK;
        }
    }

    private String generateResponseInternal(String userMessage) {
        String msg = userMessage == null ? "" : userMessage.trim();
        if (msg.isBlank()) return FALLBACK;

        String prompt = """
                You are a helpful customer support assistant for an Energy Management System.
                Answer clearly, short and professional.
                User question:
                %s
                """.formatted(msg);

        OllamaRequestDTO request = new OllamaRequestDTO(model, prompt);

        OllamaResponseDTO response = webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaResponseDTO.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

        if (response == null || response.getResponse() == null) return FALLBACK;
        return response.getResponse();
    }
}
