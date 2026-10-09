package com.example.loadbalancing.consumer;

import com.example.loadbalancing.service.LoadBalancingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DeviceDataConsumer {

    private final LoadBalancingService loadBalancingService;
    private final ObjectMapper mapper = new ObjectMapper();

    public DeviceDataConsumer(LoadBalancingService loadBalancingService) {
        this.loadBalancingService = loadBalancingService;
        System.out.println("[LB] DeviceDataConsumer READY");
    }

    @RabbitListener(queues = "${rabbitmq.device.queue}")
    public void consume(String raw) {
        try {
            JsonNode node = mapper.readTree(raw);

            // simulator trimite JSON ca STRING → decode
            if (node.isTextual()) {
                node = mapper.readTree(node.asText());
                raw = node.toString();
            }

            JsonNode deviceIdNode = node.get("deviceId");
            if (deviceIdNode == null || deviceIdNode.isNull()) {
                System.out.println("[LB] IGNORE (no deviceId)");
                return;
            }

            String deviceId = deviceIdNode.asText();
            System.out.println("[LB] RX deviceId=" + deviceId);

            loadBalancingService.forwardMessage(deviceId, raw);

        } catch (Exception e) {
            System.out.println("[LB] ERROR parsing message: " + e.getMessage());
        }
    }
}
