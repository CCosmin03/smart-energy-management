package com.example.websocket.controller;

import com.example.websocket.dto.NotificationDTO;
import com.example.websocket.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Monitoring-service → HTTP POST
    @PostMapping
    public ResponseEntity<Void> notify(@RequestBody NotificationDTO notification) {
        notificationService.sendNotification(notification);
        return ResponseEntity.ok().build();
    }
}
