package com.example.websocket.dto;

import java.time.Instant;

public class ChatMessageDTO {

    private String senderId;
    private String receiverId; // userId sau "ADMIN"
    private String senderType; // CLIENT / ADMIN
    private String content;
    private Instant timestamp;
    private String channel; // AI / ADMIN

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }


    public ChatMessageDTO() {
        this.timestamp = Instant.now();
    }

    // getters & setters

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
