package com.example.websocket.service;

import com.example.websocket.dto.ChatMessageDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatStore chatStore;

    public ChatService(SimpMessagingTemplate messagingTemplate, ChatStore chatStore) {
        this.messagingTemplate = messagingTemplate;
        this.chatStore = chatStore;
    }

    public void sendMessage(ChatMessageDTO message) {
        if (message.getChannel() == null || message.getChannel().isBlank()) {
            message.setChannel("ADMIN");
        }

        if (message == null) return;

        // 1) store in memory for later reload (inbox/history)
        chatStore.add(message);

        // 2) always notify admin topic (admin inbox live)
        messagingTemplate.convertAndSend("/topic/admin", message);

        String senderType = (message.getSenderType() == null ? "" : message.getSenderType())
                .trim().toUpperCase(Locale.ROOT);

        // 3) deliver to receiver (client) if receiverId looks like a clientId
        // IMPORTANT: do NOT send to /topic/user.ADMIN
        if (message.getReceiverId() != null && !message.getReceiverId().isBlank()
                && !"ADMIN".equalsIgnoreCase(message.getReceiverId())) {
            messagingTemplate.convertAndSend("/topic/user." + message.getReceiverId(), message);
        }

        // 4) echo back to client sender, so client sees its own message via WS (no optimistic UI needed)
        if ("CLIENT".equals(senderType) && message.getSenderId() != null && !message.getSenderId().isBlank()) {
            messagingTemplate.convertAndSend("/topic/user." + message.getSenderId(), message);
        }
    }
}
