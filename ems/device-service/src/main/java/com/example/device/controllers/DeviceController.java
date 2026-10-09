package com.example.device.controllers;

import com.example.device.dtos.DeviceDTO;
import com.example.device.dtos.DeviceDetailsDTO;
import com.example.device.services.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    // === GET ALL DEVICES (ADMIN) ===
    @GetMapping
    public List<DeviceDTO> getDevices(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can see all devices.");
        }
        return service.findDevices();
    }

    // === NEW: DEVICES FOR A SPECIFIC USER (CLIENT DASHBOARD) ===
    @GetMapping("/user/{userId}")
    public List<DeviceDTO> getDevicesForUser(@PathVariable UUID userId,
                                             @RequestHeader("X-User-Id") String headerUserId,
                                             @RequestHeader("X-User-Role") String role) {

        // daca e CLIENT, poate vedea DOAR propriile device-uri
        if ("CLIENT".equalsIgnoreCase(role)) {
            UUID currentUser = UUID.fromString(headerUserId);
            if (!currentUser.equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Clients can only see their own devices.");
            }
        } else if (!"ADMIN".equalsIgnoreCase(role)
                && !"EMPLOYEE".equalsIgnoreCase(role)
                && !"MANAGER".equalsIgnoreCase(role)) {
            // orice alt rol necunoscut este blocat
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not allowed to access this endpoint.");
        }

        return service.findDevicesForUser(userId);
    }

    // === GET ONE DEVICE (ADMIN ONLY) ===
    @GetMapping("/{id}")
    public DeviceDetailsDTO getDevice(@PathVariable UUID id,
                                      @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can access this endpoint.");
        }

        return service.findDeviceById(id);
    }

    // === CREATE DEVICE ===
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody DeviceDetailsDTO dto,
                                       @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can create devices.");
        }

        UUID id = service.createDevice(dto);
        return ResponseEntity.created(URI.create("/devices/" + id)).build();
    }

    // === UPDATE DEVICE ===
    @PutMapping("/{id}")
    public DeviceDetailsDTO update(@PathVariable UUID id,
                                   @RequestBody DeviceDetailsDTO dto,
                                   @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update devices.");
        }

        return service.updateDevice(id, dto);
    }

    // === DELETE DEVICE ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete devices.");
        }

        service.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}
