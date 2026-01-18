package com.example.monitoring.client;

import com.example.monitoring.dto.NotificationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebSocketNotifyClient {

    private final WebClient webClient;

    public WebSocketNotifyClient(@Value("${websocket.service.url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public void send(NotificationDTO dto) {
        webClient.post()
                .uri("/notify")
                .bodyValue(dto)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
