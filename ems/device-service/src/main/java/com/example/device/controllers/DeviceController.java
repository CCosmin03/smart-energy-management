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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    // === GET ALL DEVICES - only ADMIN ===
    @GetMapping
    public List<DeviceDTO> getDevices(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can see all devices.");
        }
        return service.findDevices();
    }

    // === GET DEVICE BY ID - only ADMIN or device owner ===
    @GetMapping("/{id}")
    public DeviceDetailsDTO getDevice(@PathVariable UUID id,
                                      @RequestHeader("X-User-Id") String callerId,
                                      @RequestHeader("X-User-Role") String role) {
        DeviceDetailsDTO dto = service.findDeviceById(id);

        if ("ADMIN".equalsIgnoreCase(role) || (dto.getUserId() != null && dto.getUserId().toString().equals(callerId))) {
            return dto;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to device.");
    }

    // === GET DEVICES BY USER ===
    @GetMapping("/user/{userId}")
    public List<DeviceDTO> byUser(@PathVariable UUID userId,
                                  @RequestHeader("X-User-Id") String callerId,
                                  @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role) && !callerId.equals(userId.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to user devices.");
        }

        return service.findByUser(userId);
    }

    // === CREATE DEVICE - only ADMIN ===
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody DeviceDetailsDTO dto,
                                       @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can create devices.");
        }

        UUID id = service.createDevice(dto);
        return ResponseEntity.created(URI.create("/devices/" + id)).build();
    }

    // === UPDATE DEVICE - only ADMIN ===
    @PutMapping("/{id}")
    public DeviceDetailsDTO update(@PathVariable UUID id,
                                   @RequestBody DeviceDetailsDTO dto,
                                   @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update devices.");
        }

        return service.updateDevice(id, dto);
    }

    // === DELETE DEVICE - only ADMIN ===
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can delete devices.");
        }

        service.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    // === ASSIGN DEVICE - only ADMIN ===
    @PostMapping("/{id}/assign")
    public DeviceDetailsDTO assign(@PathVariable UUID id,
                                   @RequestBody Map<String, String> body,
                                   @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can assign devices.");
        }

        UUID userId = UUID.fromString(body.get("userId"));
        return service.assignToUser(id, userId);
    }

    // === UNASSIGN DEVICE - only ADMIN ===
    @PostMapping("/{id}/unassign")
    public DeviceDetailsDTO unassign(@PathVariable UUID id,
                                     @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can unassign devices.");
        }

        return service.unassign(id);
    }
}
