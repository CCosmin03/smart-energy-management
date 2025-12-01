package com.example.device.events;

public record SyncEvent(
        String eventType,
        String payload
) {}
