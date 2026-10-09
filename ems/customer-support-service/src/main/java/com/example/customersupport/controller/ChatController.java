package com.example.customersupport.controller;

import com.example.customersupport.dto.ChatRequestDTO;
import com.example.customersupport.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<Void> sendMessage(@Valid @RequestBody ChatRequestDTO request) {

        chatService.handleUserMessage(
                request.getUserId(),
                request.getMessage(),
                request.getUseRules()
        );

        return ResponseEntity.ok().build();
    }
}
