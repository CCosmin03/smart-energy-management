package com.example.device.entities;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "device_assignment")
public class DeviceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;

    private UUID deviceId;

    private Timestamp assignedAt;

    public DeviceAssignment() {}

    public DeviceAssignment(UUID userId, UUID deviceId) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.assignedAt = new Timestamp(System.currentTimeMillis());
    }

    public Long getId() { return id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public Timestamp getAssignedAt() { return assignedAt; }
}
