package com.example.device.controllers;

import com.example.device.entities.DeviceAssignment;
import com.example.device.services.DeviceAssignmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/assignments")
public class DeviceAssignmentController {

    private final DeviceAssignmentService service;

    public DeviceAssignmentController(DeviceAssignmentService service) {
        this.service = service;
    }

    // ASSIGN
    @PostMapping
    public void assign(@RequestBody Map<String, String> body) {
        UUID userId = UUID.fromString(body.get("userId"));
        UUID deviceId = UUID.fromString(body.get("deviceId"));
        service.assign(userId, deviceId);
    }
    @GetMapping
    public List<DeviceAssignment> getAll() {
        return service.getAll();
    }
    // UNASSIGN
    @DeleteMapping("/{deviceId}")
    public void unassign(@PathVariable UUID deviceId) {
        service.unassign(deviceId);
    }

    // GET DEVICES BY USER
    @GetMapping("/user/{userId}")
    public List<DeviceAssignment> byUser(@PathVariable UUID userId) {
        return service.getByUser(userId);
    }

    // GET USER BY DEVICE
    @GetMapping("/device/{deviceId}")
    public List<DeviceAssignment> byDevice(@PathVariable UUID deviceId) {
        return service.getByDevice(deviceId);
    }
}
