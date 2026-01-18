package com.example.loadbalancing.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

@Service
public class LoadBalancingService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.ingest.queue.prefix}")
    private String ingestQueuePrefix;

    @Value("${monitoring.replicas}")
    private int monitoringReplicas;

    public LoadBalancingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void forwardMessage(String deviceId, String rawJson) {
        int replicaIndex = selectReplica(deviceId);
        String targetQueue = ingestQueuePrefix + replicaIndex;

        System.out.println(
                "[LB] ROUTE deviceId=" + deviceId +
                        " -> " + targetQueue +
                        " (" + replicaIndex + "/" + monitoringReplicas + ")"
        );

        rabbitTemplate.convertAndSend(targetQueue, rawJson);
    }

    private int selectReplica(String deviceId) {
        CRC32 crc32 = new CRC32();
        crc32.update(deviceId.getBytes(StandardCharsets.UTF_8));
        long hash = crc32.getValue();

        return (int) (hash % monitoringReplicas) + 1;
    }
}
