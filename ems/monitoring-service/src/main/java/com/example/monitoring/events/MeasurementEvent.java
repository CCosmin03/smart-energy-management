package com.example.monitoring.events;

import java.util.UUID;

public record MeasurementEvent(
        UUID deviceId,
        double consumption,
        long timestamp
) {}
