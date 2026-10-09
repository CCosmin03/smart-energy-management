package com.example.monitoring.listeners;

import com.example.monitoring.client.WebSocketNotifyClient;
import com.example.monitoring.config.RabbitConfig;
import com.example.monitoring.dto.NotificationDTO;
import com.example.monitoring.entities.HourlyConsumption;
import com.example.monitoring.events.*;
import com.example.monitoring.repositories.HourlyConsumptionRepository;
import com.example.monitoring.repositories.SyncedDeviceRepository;
import com.example.monitoring.services.SyncedDeviceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MonitoringEventListener {

    private final SyncedDeviceService deviceService;
    private final HourlyConsumptionRepository hourlyRepo;
    private final ObjectMapper mapper;
    private final SyncedDeviceRepository syncedDeviceRepo;
    private final WebSocketNotifyClient wsClient;

    // Cooldown per device (in-memory)
    private final ConcurrentHashMap<UUID, Long> lastNotifyMs = new ConcurrentHashMap<>();
    private static final long NOTIFY_COOLDOWN_MS = 30_000; // 30 sec

    public MonitoringEventListener(SyncedDeviceService deviceService,
                                   HourlyConsumptionRepository hourlyRepo,
                                   ObjectMapper mapper,
                                   SyncedDeviceRepository syncedDeviceRepo,
                                   WebSocketNotifyClient wsClient) {
        this.deviceService = deviceService;
        this.hourlyRepo = hourlyRepo;
        this.mapper = mapper;
        this.syncedDeviceRepo = syncedDeviceRepo;
        this.wsClient = wsClient;
    }

    @RabbitListener(queues = "${monitoring.ingest.queue}")
    public void handleIngestMeasurement(String rawJson) {
        try {
            MeasurementEvent dto = mapper.readValue(rawJson, MeasurementEvent.class);
            processMeasurement(dto);
        } catch (Exception e) {
            System.err.println("Error processing ingest measurement: " + e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitConfig.SYNC_QUEUE)
    public void handleSyncEvent(SyncEvent event) {
        try {
            switch (event.eventType()) {
                case "DEVICE_CREATED" -> {
                    DeviceOperationEvent dto = mapper.readValue(event.payload(), DeviceOperationEvent.class);
                    deviceService.createDevice(dto);
                }
                case "DEVICE_UPDATED" -> {
                    DeviceOperationEvent dto = mapper.readValue(event.payload(), DeviceOperationEvent.class);
                    deviceService.updateDevice(dto);
                }
                case "DEVICE_DELETED" -> {
                    DeviceIdEvent dto = mapper.readValue(event.payload(), DeviceIdEvent.class);
                    deviceService.deleteDevice(dto.deviceId());
                }
                case "MEASUREMENT_CREATED" -> {
                    MeasurementEvent dto = mapper.readValue(event.payload(), MeasurementEvent.class);
                    processMeasurement(dto);
                }
                default -> System.out.println("Monitoring ignored event: " + event.eventType());
            }
        } catch (Exception e) {
            System.err.println("Error processing monitoring event: " + e.getMessage());
        }
    }

    private void processMeasurement(MeasurementEvent dto) {
        // timestamp = millis since epoch, normalize to UTC hour
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
        double after = hc.getEnergyKwh();

        hourlyRepo.save(hc);

        // OVERCONSUMPTION CHECK + COOLDOWN
        syncedDeviceRepo.findById(dto.deviceId()).ifPresent(dev -> {
            double maxW = dev.getMaxConsumption();     // W
            double limitKwh = maxW / 1000.0;          // kWh per hour threshold (as you model it)

            if (after > limitKwh) {
                long now = System.currentTimeMillis();
                Long last = lastNotifyMs.get(dto.deviceId());

                if (last == null || now - last > NOTIFY_COOLDOWN_MS) {
                    lastNotifyMs.put(dto.deviceId(), now);

                    NotificationDTO n = new NotificationDTO();
                    n.setDeviceId(dto.deviceId().toString());
                    n.setMessage(
                            "Overconsumption detected: " + after + " kWh > " + limitKwh +
                                    " kWh (max=" + maxW + " W, device=" + dev.getName() + ")"
                    );

                    System.out.println(">>> OVERCONSUMPTION NOTIFY: " + n.getMessage());
                    wsClient.send(n);
                }
            }
        });
    }
}
