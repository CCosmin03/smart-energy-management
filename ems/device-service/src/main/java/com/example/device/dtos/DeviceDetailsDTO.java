package com.example.device.dtos;

import java.util.UUID;

public class DeviceDetailsDTO {

    private UUID id;
    private String name;
    private String status;
    private Double maxConsumption;

    public DeviceDetailsDTO() {}

    public DeviceDetailsDTO(UUID id, String name, String status, Double maxConsumption) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.maxConsumption = maxConsumption;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getMaxConsumption() {
        return maxConsumption;
    }

    public void setMaxConsumption(Double maxConsumption) {
        this.maxConsumption = maxConsumption;
    }
}
