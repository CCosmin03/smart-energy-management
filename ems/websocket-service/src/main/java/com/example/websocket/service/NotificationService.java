package com.example.websocket.service;

import com.example.websocket.dto.ControlCommandDTO;
import com.example.websocket.dto.NotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(NotificationDTO notification) {
        // 1) UI notifications
        messagingTemplate.convertAndSend("/topic/notifications", notification);

        // 2) Simulator control: STOP device simulator
        // Topic dedicated to a specific device
        ControlCommandDTO cmd = new ControlCommandDTO(notification.getDeviceId(), "STOP");
        messagingTemplate.convertAndSend("/topic/simulator/stop/" + notification.getDeviceId(), cmd);

        System.out.println(">>> WS published STOP for device " + notification.getDeviceId());
    }
}
