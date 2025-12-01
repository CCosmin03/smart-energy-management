package com.example.monitoring.events;

public record SyncEvent(
        String eventType,
        String payload
) {}
