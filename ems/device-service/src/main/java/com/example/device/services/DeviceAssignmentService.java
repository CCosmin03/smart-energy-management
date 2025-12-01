package com.example.device.services;

import com.example.device.entities.DeviceAssignment;
import com.example.device.repositories.DeviceAssignmentRepository;
import com.example.device.repositories.DeviceRepository;
import com.example.device.repositories.SyncedUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceAssignmentService {

    private final DeviceAssignmentRepository repo;
    private final SyncedUserRepository syncedUserRepository;
    private final DeviceRepository deviceRepository;

    public DeviceAssignmentService(DeviceAssignmentRepository repo,
                                   SyncedUserRepository syncedUserRepository,
                                   DeviceRepository deviceRepository) {
        this.repo = repo;
        this.syncedUserRepository = syncedUserRepository;
        this.deviceRepository = deviceRepository;
    }

    @Transactional
    public void assign(UUID userId, UUID deviceId) {

        // 1. validate user
        if (!syncedUserRepository.existsById(userId)) {
            throw new RuntimeException("User does not exist in synced_users: " + userId);
        }

        // 2. validate device
        if (!deviceRepository.existsById(deviceId)) {
            throw new RuntimeException("Device does not exist: " + deviceId);
        }

        // 3. optional: prevent duplicate assignment
        List<DeviceAssignment> existing = repo.findByDeviceId(deviceId);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Device is already assigned: " + deviceId);
        }

        // 4. save assignment
        repo.save(new DeviceAssignment(userId, deviceId));
    }

    @Transactional
    public void unassign(UUID deviceId) {
        repo.deleteByDeviceId(deviceId);
    }

    public List<DeviceAssignment> getAll() {
        return repo.findAll();
    }

    public List<DeviceAssignment> getByUser(UUID userId) {
        return repo.findByUserId(userId);
    }

    public List<DeviceAssignment> getByDevice(UUID deviceId) {
        return repo.findByDeviceId(deviceId);
    }
}
