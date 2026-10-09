package com.example.monitoring.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "synced_devices")
public class SyncedDevice {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    private String name;
    private String status;
    private int maxConsumption;

    public SyncedDevice() {}

    public SyncedDevice(UUID id, String name, String status, int maxConsumption) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.maxConsumption = maxConsumption;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public int getMaxConsumption() {
        return maxConsumption;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMaxConsumption(int maxConsumption) {
        this.maxConsumption = maxConsumption;
    }
}
