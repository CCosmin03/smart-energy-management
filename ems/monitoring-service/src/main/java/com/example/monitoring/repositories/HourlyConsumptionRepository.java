package com.example.monitoring.repositories;

import com.example.monitoring.entities.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, UUID> {

    List<HourlyConsumption> findByDeviceIdAndHourTimestampBetween(
            UUID deviceId,
            LocalDateTime start,
            LocalDateTime end
    );
}
