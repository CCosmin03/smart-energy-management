package com.example.device.events;

import java.util.UUID;

public record DeviceOperationEvent(
        UUID id,
        String name,
        String status,
        Double maxConsumption
) {}
