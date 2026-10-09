package com.example.user.events;

import java.util.UUID;

public record UserOperationEvent(
        UUID id,
        String name,
        String address,
        int age
) {}
