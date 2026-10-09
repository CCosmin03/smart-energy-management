package com.example.monitoring.config;

import com.example.monitoring.events.MeasurementEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.monitoring.events.SyncEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String SYNC_EXCHANGE = "sync.exchange";
    public static final String SYNC_QUEUE = "sync.queue.monitoring";


    @Bean
    public FanoutExchange syncExchange() {
        return new FanoutExchange(SYNC_EXCHANGE);
    }

    @Bean
    public Queue syncQueue() {
        return QueueBuilder.durable(SYNC_QUEUE).build();
    }

    @Bean
    public Binding bindingSyncQueue(FanoutExchange syncExchange, Queue syncQueue) {
        return BindingBuilder.bind(syncQueue).to(syncExchange);
    }

    @Bean
    public Queue ingestQueue(@Value("${monitoring.ingest.queue}") String ingestQueueName) {
        return QueueBuilder.durable(ingestQueueName).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.example.*");

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.example.user.events.SyncEvent", SyncEvent.class);
        idClassMapping.put("com.example.device.events.SyncEvent", SyncEvent.class);
        idClassMapping.put("com.example.simulator.events.SyncEvent", SyncEvent.class);

        classMapper.setIdClassMapping(idClassMapping);

        converter.setClassMapper(classMapper);
        return converter;
    }
}
