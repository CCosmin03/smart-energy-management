package com.example.monitoring.listeners;

import com.example.monitoring.config.RabbitConfig;
import com.example.monitoring.entities.HourlyConsumption;
import com.example.monitoring.events.*;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import com.example.monitoring.services.SyncedDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class MonitoringEventListener {

    private final SyncedDeviceService deviceService;
    private final HourlyConsumptionRepository hourlyRepo;
    private final ObjectMapper mapper;

    public MonitoringEventListener(SyncedDeviceService deviceService,
                                   HourlyConsumptionRepository hourlyRepo,
                                   ObjectMapper mapper) {
        this.deviceService = deviceService;
        this.hourlyRepo = hourlyRepo;
        this.mapper = mapper;
    }

    @RabbitListener(queues = RabbitConfig.SYNC_QUEUE)
    public void handleSyncEvent(SyncEvent event) {
        try {
            switch (event.eventType()) {
                case "DEVICE_CREATED" -> {
                    DeviceOperationEvent dto =
                            mapper.readValue(event.payload(), DeviceOperationEvent.class);
                    deviceService.createDevice(dto);
                }

                case "DEVICE_UPDATED" -> {
                    DeviceOperationEvent dto =
                            mapper.readValue(event.payload(), DeviceOperationEvent.class);
                    deviceService.updateDevice(dto);
                }

                case "DEVICE_DELETED" -> {
                    DeviceIdEvent dto =
                            mapper.readValue(event.payload(), DeviceIdEvent.class);
                    deviceService.deleteDevice(dto.deviceId());
                }

                case "MEASUREMENT_CREATED" -> {
                    MeasurementEvent dto =
                            mapper.readValue(event.payload(), MeasurementEvent.class);
                    processMeasurement(dto);
                }

                default -> System.out.println("Monitoring ignored event: " + event.eventType());
            }
        } catch (Exception e) {
            System.err.println("Error processing monitoring event: " + e.getMessage());
        }
    }

    private void processMeasurement(MeasurementEvent dto) {
        // timestamp = millis since epoch
        LocalDateTime ts = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(dto.timestamp()),
                ZoneOffset.UTC
        );

        LocalDateTime hour = ts.withMinute(0).withSecond(0).withNano(0);

        List<HourlyConsumption> list =
                hourlyRepo.findByDeviceIdAndHourTimestampBetween(
                        dto.deviceId(),
                        hour,
                        hour.plusHours(1)
                );

        HourlyConsumption hc =
                list.isEmpty()
                        ? new HourlyConsumption(dto.deviceId(), hour, 0)
                        : list.get(0);

        hc.add(dto.consumption());
        hourlyRepo.save(hc);
    }
}
