package com.example.websocket.controller;

import com.example.websocket.dto.ChatMessageDTO;
import com.example.websocket.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void send(ChatMessageDTO message) {
        chatService.sendMessage(message);
    }
}
