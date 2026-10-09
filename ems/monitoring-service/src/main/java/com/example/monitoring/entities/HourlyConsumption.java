package com.example.monitoring.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hourly_consumption")
public class HourlyConsumption {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "hour_timestamp", nullable = false)
    private LocalDateTime hourTimestamp;

    @Column(name = "energy_kwh", nullable = false)
    private double energyKwh;


    public HourlyConsumption() {
        this.id = UUID.randomUUID();
    }

    public HourlyConsumption(UUID deviceId, LocalDateTime hourTimestamp, double energyKwh) {
        this.id = UUID.randomUUID();
        this.deviceId = deviceId;
        this.hourTimestamp = hourTimestamp;
        this.energyKwh = energyKwh;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public LocalDateTime getHourTimestamp() {
        return hourTimestamp;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public void add(double valueKwh) {
        this.energyKwh += valueKwh;
    }
}
