package com.example.monitoring.events;

import java.util.UUID;

public record DeviceOperationEvent(
        UUID id,
        String name,
        String status,
        int maxConsumption
) {}
