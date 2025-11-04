package com.example.device.services;

import com.example.device.dtos.DeviceDTO;
import com.example.device.dtos.DeviceDetailsDTO;
import com.example.device.dtos.builders.DeviceBuilder;
import com.example.device.entities.Device;
import com.example.device.repositories.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    // LIST
    public List<DeviceDTO> findDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    // GET ONE
    public DeviceDetailsDTO findDeviceById(UUID id) {
        Device d = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found: " + id));
        return DeviceBuilder.toDeviceDetailsDTO(d);
    }

    // CREATE
    public UUID createDevice(DeviceDetailsDTO dto) {
        Device d = DeviceBuilder.toEntity(dto);
        // id va fi generat de @UuidGenerator
        d = deviceRepository.save(d);
        return d.getId();
    }

    // UPDATE (full)
    public DeviceDetailsDTO updateDevice(UUID id, DeviceDetailsDTO dto) {
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        existing.setName(dto.getName());
        existing.setStatus(dto.getStatus());
        existing.setUserId(dto.getUserId());

        existing = deviceRepository.save(existing);
        return DeviceBuilder.toDeviceDetailsDTO(existing);
    }

    // DELETE
    public void deleteDevice(UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new RuntimeException("Device not found: " + id);
        }
        deviceRepository.deleteById(id);
    }

    // ASSIGN
    public DeviceDetailsDTO assignToUser(UUID deviceId, UUID userId) {
        Device d = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));
        d.setUserId(userId);
        d = deviceRepository.save(d);
        return DeviceBuilder.toDeviceDetailsDTO(d);
    }

    // UNASSIGN
    public DeviceDetailsDTO unassign(UUID deviceId) {
        Device d = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));
        d.setUserId(null);
        d = deviceRepository.save(d);
        return DeviceBuilder.toDeviceDetailsDTO(d);
    }

    // BY USER
    public List<DeviceDTO> findByUser(UUID userId) {
        return deviceRepository.findByUserId(userId)
                .stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }
}
