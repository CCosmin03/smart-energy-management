package com.example.monitoring.repositories;

import com.example.monitoring.entities.SyncedDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SyncedDeviceRepository extends JpaRepository<SyncedDevice, UUID> {}
