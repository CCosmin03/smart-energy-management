package com.example.websocket.controller;

import com.example.websocket.dto.ChatMessageDTO;
import com.example.websocket.service.ChatService;
import com.example.websocket.service.ChatStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notify/chat")
public class ChatNotificationController {

    private final ChatService chatService;
    private final ChatStore chatStore;

    public ChatNotificationController(ChatService chatService, ChatStore chatStore) {
        this.chatService = chatService;
        this.chatStore = chatStore;
    }

    @PostMapping
    public ResponseEntity<Void> forwardChatMessage(@RequestBody ChatMessageDTO message) {
        chatService.sendMessage(message);
        return ResponseEntity.ok().build();
    }

    // /notify/chat/inbox?channel=ADMIN (default ADMIN)
    @GetMapping("/inbox")
    public ResponseEntity<List<ChatMessageDTO>> inbox(
            @RequestParam(name = "channel", required = false, defaultValue = "ADMIN") String channel
    ) {
        return ResponseEntity.ok(chatStore.inbox(channel));
    }

    // /notify/chat/history/{clientId}?channel=ADMIN (default ADMIN)
    @GetMapping("/history/{clientId}")
    public ResponseEntity<List<ChatMessageDTO>> history(
            @PathVariable String clientId,
            @RequestParam(name = "channel", required = false, defaultValue = "ADMIN") String channel
    ) {
        return ResponseEntity.ok(chatStore.history(clientId, channel));
    }
}
