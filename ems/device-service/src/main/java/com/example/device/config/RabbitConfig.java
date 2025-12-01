package com.example.device.config;

import com.example.device.events.SyncEvent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String SYNC_EXCHANGE = "sync.exchange";
    public static final String SYNC_QUEUE = "sync.queue.device";

    @Bean
    public FanoutExchange syncExchange() {
        return new FanoutExchange(SYNC_EXCHANGE);
    }

    @Bean
    public Queue syncQueue() {
        return new Queue(SYNC_QUEUE, false);
    }

    @Bean
    public Binding bindingSyncQueue(FanoutExchange syncExchange, Queue syncQueue) {
        return BindingBuilder.bind(syncQueue).to(syncExchange);
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
