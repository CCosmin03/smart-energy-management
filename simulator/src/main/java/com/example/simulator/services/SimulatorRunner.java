package com.example.simulator.services;

import com.example.simulator.config.RabbitConfig;
import com.example.simulator.events.MeasurementEvent;
import com.example.simulator.events.SyncEvent;
import com.example.simulator.events.SimulatorStopCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SimulatorRunner implements CommandLineRunner {

    private final RabbitTemplate template;
    private final UUID deviceId;
    private final long interval;
    private final Random random = new Random();
    private final ObjectMapper mapper = new ObjectMapper();

    private final AtomicBoolean running = new AtomicBoolean(true);

    // SockJS endpoint: http://localhost:8085/ws
    private final String wsUrl;

    public SimulatorRunner(
            RabbitTemplate template,
            @Value("${sim.device-id}") UUID deviceId,
            @Value("${sim.interval}") long interval,
            @Value("${sim.ws-url}") String wsUrl
    ) {
        this.template = template;
        this.deviceId = deviceId;
        this.interval = interval;
        this.wsUrl = wsUrl;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== SIMULATOR STARTED for device: " + deviceId + " ===");

        // Connect to websocket-service (SockJS) and listen for STOP
        connectStopListenerSockJs();

        // Optional test event
        try {
            template.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    new SyncEvent("TEST", "\"Hello\"")
            );
            System.out.println(">>> SENT TEST EVENT <<<");
        } catch (Exception e) {
            System.out.println("SIM ERROR: " + e.getMessage());
            e.printStackTrace();
        }

        while (running.get()) {
            double powerW = generateBasePowerW() + random.nextDouble() * 2.0; // small noise W
            double kwh = powerW / 1000.0 * (interval / 3600000.0);            // W -> kWh over interval

            MeasurementEvent measurement = new MeasurementEvent(
                    deviceId,
                    kwh,
                    System.currentTimeMillis()
            );

            String payload = mapper.writeValueAsString(measurement);

            // 1) Trimite in continuare pe SYNC_EXCHANGE (ca sa nu stricam ce merge)
            SyncEvent event = new SyncEvent("MEASUREMENT_CREATED", payload);

            template.convertAndSend(
                    RabbitConfig.SYNC_EXCHANGE,
                    "",
                    event
            );

            // 2) Trimite si direct in device_data_queue pentru Load Balancing
            // LB-ul consuma de aici si face forward in ingest-x
            template.convertAndSend(
                    RabbitConfig.DEVICE_DATA_QUEUE, // "device_data_queue"
                    payload
            );

            System.out.println("[SIMULATOR] Sent: " + kwh + " kWh (" + powerW + " W) "
                    + " | sync=MEASUREMENT_CREATED + dataQueue=" + RabbitConfig.DEVICE_DATA_QUEUE);

            Thread.sleep(interval);
        }

        System.out.println("=== SIMULATOR STOPPED (received STOP command) ===");
        System.exit(0);
    }

    private void connectStopListenerSockJs() {
        try {
            WebSocketClient webSocketClient = new StandardWebSocketClient();
            Transport webSocketTransport = new WebSocketTransport(webSocketClient);
            SockJsClient sockJsClient = new SockJsClient(List.of(webSocketTransport));

            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {

                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    String topic = "/topic/simulator/stop/" + deviceId;
                    System.out.println(">>> Connected to WS (SockJS). Subscribing: " + topic);

                    session.subscribe(topic, new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return SimulatorStopCommand.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            SimulatorStopCommand cmd = (SimulatorStopCommand) payload;
                            System.out.println(">>> COMMAND RECEIVED: " + cmd.getCommand()
                                    + " device=" + cmd.getDeviceId());

                            if ("STOP".equalsIgnoreCase(cmd.getCommand())) {
                                running.set(false);
                            }
                        }
                    });
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    System.out.println("WS transport error: " + exception.getMessage());
                }

                @Override
                public void handleException(StompSession session, StompCommand command,
                                            StompHeaders headers, byte[] payload, Throwable exception) {
                    System.out.println("WS exception: " + exception.getMessage());
                }
            };

            System.out.println(">>> Connecting to websocket-service: " + wsUrl);
            ListenableFuture<StompSession> f = stompClient.connect(wsUrl, sessionHandler);
            f.get();
        } catch (Exception e) {
            System.out.println(">>> Could not connect to websocket-service for STOP commands: " + e.getMessage());
        }
    }

    private double generateBasePowerW() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 6) return 200;
        if (hour < 12) return 600;
        if (hour < 18) return 720;
        return 800;
    }
}
