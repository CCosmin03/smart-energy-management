package com.example.monitoring.repositories;

import com.example.monitoring.entities.HourlyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HourlyConsumptionRepository extends JpaRepository<HourlyConsumption, UUID> {

    // Pentru “un singur rand pe ora”
    Optional<HourlyConsumption> findFirstByDeviceIdAndHourTimestamp(UUID deviceId, LocalDateTime hourTimestamp);

    // Pentru grafice zilnice: interval [start, end)
    List<HourlyConsumption> findByDeviceIdAndHourTimestampGreaterThanEqualAndHourTimestampLessThan(
            UUID deviceId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<HourlyConsumption> findByDeviceIdAndHourTimestampBetween(UUID uuid, LocalDateTime hour, LocalDateTime localDateTime);
}
