package com.example.customersupport.client;

import com.example.customersupport.dto.ChatMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebSocketClient {

    private final WebClient webClient;

    public WebSocketClient(
            @Value("${websocket.service.url}") String websocketServiceUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(websocketServiceUrl)
                .build();
    }

    public void sendMessage(ChatMessageDTO message) {
        webClient.post()
                .uri("/notify/chat")
                .bodyValue(message)
                .retrieve()
                .toBodilessEntity()
                .timeout(java.time.Duration.ofSeconds(3))
                .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                .block();
    }
}
