package com.example.simulator.events;

import java.util.UUID;

public record MeasurementEvent(
        UUID deviceId,
        double consumption,
        long timestamp
) {}
