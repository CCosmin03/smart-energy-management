package com.example.device.dtos;

import java.util.Objects;
import java.util.UUID;

public class DeviceDTO {

    private UUID id;
    private String name;
    private String status;
    private UUID userId; // <--- ADĂUGAT

    public DeviceDTO() {}

    public DeviceDTO(UUID id, String name, String status, UUID userId) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceDTO that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, status);
    }
}
