package com.example.device.services;

import com.example.device.dtos.DeviceDTO;
import com.example.device.dtos.DeviceDetailsDTO;
import com.example.device.dtos.builders.DeviceBuilder;
import com.example.device.entities.Device;
import com.example.device.entities.DeviceAssignment;
import com.example.device.events.DeviceIdEvent;
import com.example.device.repositories.DeviceAssignmentRepository;
import com.example.device.repositories.DeviceRepository;
import com.example.device.config.RabbitConfig;

import com.example.device.events.DeviceOperationEvent;
import com.example.device.events.SyncEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    public DeviceService(DeviceRepository deviceRepository,
                         DeviceAssignmentRepository assignmentRepository,
                         RabbitTemplate rabbitTemplate,
                         ObjectMapper mapper) {
        this.deviceRepository = deviceRepository;
        this.assignmentRepository = assignmentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
    }

    public List<DeviceDTO> findDevices() {
        return deviceRepository.findAll()
                .stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Device d = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found: " + id));
        return DeviceBuilder.toDeviceDetailsDTO(d);
    }

    public UUID createDevice(DeviceDetailsDTO dto) {
        Device d = DeviceBuilder.toEntity(dto);
        d = deviceRepository.save(d);


        try {
            DeviceOperationEvent devEvent = new DeviceOperationEvent(
                    d.getId(),
                    d.getName(),
                    d.getStatus(),
                    d.getMaxConsumption()
            );

            String payload = mapper.writeValueAsString(devEvent);

            SyncEvent event = new SyncEvent("DEVICE_CREATED", payload);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    event
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish DEVICE_CREATED event", e);
        }

        return d.getId();
    }

    public DeviceDetailsDTO updateDevice(UUID id, DeviceDetailsDTO dto) {
        Device existing = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found: " + id));

        existing.setName(dto.getName());
        existing.setStatus(dto.getStatus());
        existing.setMaxConsumption(dto.getMaxConsumption());

        existing = deviceRepository.save(existing);

        try {
            DeviceOperationEvent devEvent = new DeviceOperationEvent(
                    existing.getId(),
                    existing.getName(),
                    existing.getStatus(),
                    existing.getMaxConsumption()
            );

            String payload = mapper.writeValueAsString(devEvent);

            SyncEvent event = new SyncEvent("DEVICE_UPDATED", payload);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    event
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish DEVICE_UPDATED event", e);
        }

        return DeviceBuilder.toDeviceDetailsDTO(existing);
    }


    public void deleteDevice(UUID id) {
        if (!deviceRepository.existsById(id)) {
            throw new RuntimeException("Device not found: " + id);
        }

        deviceRepository.deleteById(id);

        try {
            DeviceIdEvent dto = new DeviceIdEvent(id);

            String payload = mapper.writeValueAsString(dto);

            SyncEvent event = new SyncEvent("DEVICE_DELETED", payload);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    event
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish DEVICE_DELETED event", e);
        }
    }


    public List<DeviceDTO> findDevicesForUser(UUID userId) {

        List<DeviceAssignment> assignments = assignmentRepository.findByUserId(userId);
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<UUID> deviceIds = assignments.stream()
                .map(DeviceAssignment::getDeviceId)
                .toList();

        List<Device> devices = deviceRepository.findAllById(deviceIds);

        return devices.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

}
