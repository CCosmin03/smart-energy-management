package com.example.monitoring.services;

import com.example.monitoring.entities.SyncedDevice;
import com.example.monitoring.events.DeviceOperationEvent;
import com.example.monitoring.repositories.SyncedDeviceRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SyncedDeviceService {

    private final SyncedDeviceRepository repo;

    public SyncedDeviceService(SyncedDeviceRepository repo) {
        this.repo = repo;
    }

    public void createDevice(DeviceOperationEvent dto) {
        repo.save(new SyncedDevice(
                dto.id(),
                dto.name(),
                dto.status(),
                dto.maxConsumption()
        ));
    }

    public void updateDevice(DeviceOperationEvent dto) {
        repo.findById(dto.id()).ifPresent(dev -> {
            dev.setName(dto.name());
            dev.setStatus(dto.status());
            dev.setMaxConsumption(dto.maxConsumption());
            repo.save(dev);
        });
    }

    public void deleteDevice(UUID id) {
        repo.deleteById(id);
    }
}
