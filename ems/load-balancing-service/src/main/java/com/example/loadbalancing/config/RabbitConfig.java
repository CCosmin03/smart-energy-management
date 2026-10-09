package com.example.loadbalancing.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.device.queue}")
    private String deviceQueueName;

    @Value("${rabbitmq.ingest.queue.prefix}")
    private String ingestQueuePrefix;

    @Value("${monitoring.replicas}")
    private int monitoringReplicas;

    // Queue-ul central din care Load Balancing Service consuma
    @Bean
    public Queue deviceDataQueue() {
        return QueueBuilder.durable(deviceQueueName).build();
    }

    // ingest-1, ingest-2, ingest-3, ...
    @Bean
    public Queue[] ingestQueues() {
        Queue[] queues = new Queue[monitoringReplicas];

        for (int i = 0; i < monitoringReplicas; i++) {
            String queueName = ingestQueuePrefix + (i + 1);
            queues[i] = QueueBuilder.durable(queueName).build();
        }
        return queues;
    }
}
