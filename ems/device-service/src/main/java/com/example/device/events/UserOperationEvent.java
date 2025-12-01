package com.example.device.events; // device-service va avea package-ul device.events

import java.util.UUID;

public record UserOperationEvent(
        UUID id,
        String name,
        String address,
        int age
) {}
