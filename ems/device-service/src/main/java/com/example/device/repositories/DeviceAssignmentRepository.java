package com.example.device.repositories;

import com.example.device.entities.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, Long> {

    List<DeviceAssignment> findByUserId(UUID userId);

    List<DeviceAssignment> findByDeviceId(UUID deviceId);

    void deleteByDeviceId(UUID deviceId);

    void deleteByUserId(UUID userId);
}
