package com.example.monitoring.controllers;

import com.example.monitoring.services.MonitoringService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringService service;

    public MonitoringController(MonitoringService service) {
        this.service = service;
    }

    @GetMapping("/device/{deviceId}")
    public List<Double> getDaily(@PathVariable UUID deviceId,
                                 @RequestParam String date) {

        LocalDate parsed = LocalDate.parse(date);

        return service.getDailyConsumption(deviceId, parsed);
    }
}
