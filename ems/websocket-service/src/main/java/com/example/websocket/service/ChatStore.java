package com.example.websocket.service;

import com.example.websocket.dto.ChatMessageDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatStore {

    private static final int MAX_PER_CONVERSATION = 200;

    // key = clientId|CHANNEL
    private final Map<String, Deque<ChatMessageDTO>> conversations = new ConcurrentHashMap<>();

    private String norm(String s) {
        return s == null ? "" : s.trim();
    }

    private String channelOf(ChatMessageDTO m) {
        String ch = norm(m.getChannel()).toUpperCase(Locale.ROOT);
        if (ch.isBlank()) return "ADMIN"; // default safe
        return ch;
    }

    private String clientIdOf(ChatMessageDTO m) {
        if (m == null) return "";
        String senderType = norm(m.getSenderType()).toUpperCase(Locale.ROOT);

        if ("CLIENT".equals(senderType)) return norm(m.getSenderId());
        return norm(m.getReceiverId());
    }

    private String keyOf(ChatMessageDTO m) {
        String clientId = clientIdOf(m);
        String ch = channelOf(m);
        if (clientId.isBlank()) return "";
        return clientId + "|" + ch;
    }

    private String keyOf(String clientId, String channel) {
        String cid = norm(clientId);
        String ch = norm(channel).toUpperCase(Locale.ROOT);
        if (ch.isBlank()) ch = "ADMIN";
        if (cid.isBlank()) return "";
        return cid + "|" + ch;
    }

    public void add(ChatMessageDTO m) {
        String key = keyOf(m);
        if (key.isBlank()) return;

        conversations.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(m);

        Deque<ChatMessageDTO> q = conversations.get(key);
        while (q.size() > MAX_PER_CONVERSATION) q.removeFirst();
    }

    public List<ChatMessageDTO> history(String clientId, String channel) {
        String key = keyOf(clientId, channel);
        if (key.isBlank()) return List.of();
        Deque<ChatMessageDTO> q = conversations.get(key);
        if (q == null) return List.of();
        return new ArrayList<>(q); // chronological
    }

    public List<ChatMessageDTO> inbox(String channel) {
        String ch = norm(channel).toUpperCase(Locale.ROOT);
        if (ch.isBlank()) ch = "ADMIN";

        List<ChatMessageDTO> last = new ArrayList<>();

        for (var entry : conversations.entrySet()) {
            String key = entry.getKey(); // clientId|CHANNEL
            if (!key.endsWith("|" + ch)) continue;

            Deque<ChatMessageDTO> q = entry.getValue();
            if (q != null && !q.isEmpty()) last.add(q.getLast());
        }

        last.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return last;
    }
}
