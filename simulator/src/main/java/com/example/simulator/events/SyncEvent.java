package com.example.simulator.events;

public record SyncEvent(
        String eventType,
        String payload
) {}
