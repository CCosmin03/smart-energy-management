package com.example.simulator.services;

import com.example.simulator.config.RabbitConfig;
import com.example.simulator.events.MeasurementEvent;
import com.example.simulator.events.SyncEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class SimulatorRunner implements CommandLineRunner {

    private final RabbitTemplate template;
    private final UUID deviceId;
    private final long interval;
    private final Random random = new Random();
    private final ObjectMapper mapper = new ObjectMapper();

    public SimulatorRunner(
            RabbitTemplate template,
            @Value("${sim.device-id}") UUID deviceId,
            @Value("${sim.interval}") long interval
    ) {
        this.template = template;
        this.deviceId = deviceId;
        this.interval = interval;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== SIMULATOR STARTED for device: " + deviceId + " ===");

        // TEST CONNECTION FIRST
        try {
            template.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    new SyncEvent("TEST", "\"Hello\"")
            );
            System.out.println(">>> SENT TEST EVENT <<<");
        } catch (Exception e) {
            System.out.println("SIM ERROR: " + e.getMessage());
            e.printStackTrace();
        }


        while (true) {

            double base = generateBaseConsumption();
            double noise = random.nextDouble() * 0.2;
            double value = base + noise;

            MeasurementEvent measurement = new MeasurementEvent(
                    deviceId,
                    value,
                    System.currentTimeMillis()
            );

            String payload = mapper.writeValueAsString(measurement);

            SyncEvent event = new SyncEvent(
                    "MEASUREMENT_CREATED",
                    payload
            );

            template.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    event
            );

            System.out.println("[SIMULATOR] Sent: " + value + " kWh");

            Thread.sleep(interval);
        }
    }

    private double generateBaseConsumption() {
        int hour = java.time.LocalDateTime.now().getHour();

        if (hour < 6) return 0.1;   // night
        if (hour < 12) return 0.3;  // morning
        if (hour < 18) return 0.5;  // afternoon
        return 0.4;                 // evening
    }
}
