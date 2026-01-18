package com.example.monitoring.controllers;

import com.example.monitoring.services.MonitoringService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringService service;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;          // yyyy-MM-dd
    private static final DateTimeFormatter DOT = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // dd.MM.yyyy

    public MonitoringController(MonitoringService service) {
        this.service = service;
    }

    @GetMapping("/device/{deviceId}")
    public List<Double> getDaily(@PathVariable UUID deviceId,
                                 @RequestParam String date) {

        LocalDate parsed = parseDate(date);
        return service.getDailyConsumption(deviceId, parsed);
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, ISO);
        } catch (DateTimeParseException ignored) {
            // fallback: dd.MM.yyyy
            return LocalDate.parse(s, DOT);
        }
    }
}
