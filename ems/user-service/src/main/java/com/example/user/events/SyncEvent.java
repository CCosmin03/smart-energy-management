package com.example.user.events;

public record SyncEvent(
        String eventType,
        String payload
) {}
