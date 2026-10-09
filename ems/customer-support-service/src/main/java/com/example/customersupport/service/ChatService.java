package com.example.customersupport.service;

import com.example.customersupport.client.WebSocketClient;
import com.example.customersupport.dto.ChatMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {

    private final RuleBasedChatbotService ruleBasedChatbotService;
    private final AiChatbotService aiChatbotService;
    private final WebSocketClient webSocketClient;

    // global toggle (env): CHAT_RULES_ENABLED=true/false
    private final boolean rulesEnabledGlobal;

    // userId -> ACK sent once
    private final Set<String> adminAckSent = ConcurrentHashMap.newKeySet();

    public ChatService(RuleBasedChatbotService ruleBasedChatbotService,
                       AiChatbotService aiChatbotService,
                       WebSocketClient webSocketClient,
                       @Value("${chat.rules.enabled:true}") boolean rulesEnabledGlobal) {
        this.ruleBasedChatbotService = ruleBasedChatbotService;
        this.aiChatbotService = aiChatbotService;
        this.webSocketClient = webSocketClient;
        this.rulesEnabledGlobal = rulesEnabledGlobal;

        System.out.println("[ChatService] chat.rules.enabled=" + rulesEnabledGlobal);
    }

    // useRules can be null -> fallback to global toggle
    public void handleUserMessage(String userId, String userMessage, Boolean useRules) {
        if (userId == null || userId.isBlank()) return;
        if (userMessage == null) userMessage = "";

        String msg = userMessage.trim();

        // ===================== ADMIN trigger STRICT =====================
        boolean wantsAdmin =
                msg.startsWith("[ADMIN]") ||
                        msg.toLowerCase().startsWith("/admin");

        if (wantsAdmin) {
            String clean = msg.replaceFirst("^\\[ADMIN\\]\\s*", "")
                    .replaceFirst("^/admin\\s*", "")
                    .trim();

            // ECHO user message into ADMIN channel so client sees it
            ChatMessageDTO echo = new ChatMessageDTO();
            echo.setSenderId(userId);
            echo.setReceiverId("ADMIN");
            echo.setSenderType("CLIENT");
            echo.setChannel("ADMIN");
            echo.setContent(clean);
            webSocketClient.sendMessage(echo);

            // ACK once
            if (adminAckSent.add(userId)) {
                ChatMessageDTO ack = new ChatMessageDTO();
                ack.setSenderId("BOT");
                ack.setReceiverId(userId);
                ack.setSenderType("BOT");
                ack.setChannel("ADMIN");
                ack.setContent("Am trimis mesajul tau catre un administrator. Te va contacta in cel mai scurt timp.");
                webSocketClient.sendMessage(ack);
            }
            return;
        }

        // ===================== AI channel =====================
        msg = msg.replaceFirst("^\\[AI\\]\\s*", "").trim();

        // ECHO user message into AI channel so client sees it
        ChatMessageDTO echo = new ChatMessageDTO();
        echo.setSenderId(userId);
        echo.setReceiverId("BOT");
        echo.setSenderType("CLIENT");
        echo.setChannel("AI");
        echo.setContent(msg);
        webSocketClient.sendMessage(echo);

        // Toggle per request or global
        boolean rulesEnabled = (useRules != null) ? useRules : rulesEnabledGlobal;

        String response = null;

        if (rulesEnabled) {
            response = ruleBasedChatbotService.getAutomaticResponse(msg);
        }

        if (response == null) {
            response = aiChatbotService.generateResponse(msg);
        }

        // AI failure does NOT auto-escalate to admin; it stays in AI channel
        ChatMessageDTO reply = new ChatMessageDTO();
        reply.setSenderId("BOT");
        reply.setReceiverId(userId);
        reply.setSenderType("BOT");
        reply.setChannel("AI");
        reply.setContent(response);
        webSocketClient.sendMessage(reply);
    }
}
