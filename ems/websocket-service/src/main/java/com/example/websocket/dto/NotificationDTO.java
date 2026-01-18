package com.example.websocket.dto;

import java.time.Instant;

public class NotificationDTO {

    private String deviceId;
    private String message;
    private Instant timestamp;

    public NotificationDTO() {
        this.timestamp = Instant.now();
    }

    // getters & setters

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
