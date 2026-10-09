package com.example.monitoring.services;

import com.example.monitoring.entities.HourlyConsumption;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class MonitoringService {

    private final HourlyConsumptionRepository repo;

    public MonitoringService(HourlyConsumptionRepository repo) {
        this.repo = repo;
    }

    public List<Double> getDailyConsumption(UUID deviceId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<HourlyConsumption> rows =
                repo.findByDeviceIdAndHourTimestampGreaterThanEqualAndHourTimestampLessThan(deviceId, start, end);

        double[] hours = new double[24];

        for (HourlyConsumption hc : rows) {
            int h = hc.getHourTimestamp().getHour();
            hours[h] = hc.getEnergyKwh();
        }

        return Arrays.stream(hours).boxed().toList();
    }
}
