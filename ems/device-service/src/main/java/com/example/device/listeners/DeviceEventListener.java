package com.example.device.listeners;

import com.example.device.config.RabbitConfig;
import com.example.device.events.SyncEvent;
import com.example.device.events.UserIdEvent;
import com.example.device.events.UserOperationEvent;
import com.example.device.services.DeviceAssignmentService;
import com.example.device.services.SyncedUserService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DeviceEventListener {

    private final SyncedUserService syncedUserService;
    private final DeviceAssignmentService assignmentService;
    private final ObjectMapper mapper;

    public DeviceEventListener(SyncedUserService syncedUserService,
                               DeviceAssignmentService assignmentService,
                               ObjectMapper mapper) {
        this.syncedUserService = syncedUserService;
        this.assignmentService = assignmentService;
        this.mapper = mapper;
    }

    @RabbitListener(queues = RabbitConfig.SYNC_QUEUE)
    public void handleSyncEvent(SyncEvent event) {
        try {
            switch (event.eventType()) {

                case "USER_CREATED" -> {
                    UserOperationEvent dto =
                            mapper.readValue(event.payload(), UserOperationEvent.class);
                    syncedUserService.createUser(dto);
                }

                case "USER_DELETED" -> {
                    UserIdEvent dto =
                            mapper.readValue(event.payload(), UserIdEvent.class);

                    // 1. stergem asignarile device-ului
                    assignmentService.unassign(dto.userId());

                    // 2. stergem userul din tabela locala
                    syncedUserService.deleteUser(dto);
                }

                case "USER_UPDATED" -> {
                    UserOperationEvent dto =
                            mapper.readValue(event.payload(), UserOperationEvent.class);

                    syncedUserService.updateUser(dto);   // trebuie să implementăm metoda!
                }


                default -> System.out.println("Device-service ignored: " + event.eventType());
            }

        } catch (Exception e) {
            System.err.println("Error processing sync event in device-service: " + e.getMessage());
        }
    }
}
