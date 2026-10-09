# Energy Management System – Part III
## Load Balancing, WebSocket Notifications, Distributed Monitoring

This project represents the third stage of the distributed Energy Management System.  
It extends the previous microservice-based architecture with **load balancing**, **replicated monitoring services**, and **real-time WebSocket notifications** for overconsumption events.

The system is composed of independent microservices, each with its own PostgreSQL database, communicating through HTTP, RabbitMQ, and WebSockets.  
Traefik acts as a reverse proxy and authentication gateway, while RabbitMQ is used both for synchronization and for distributing measurement data across replicated services.

---

## General Architecture

Traefik receives all incoming HTTP requests, validates JWT tokens using the ForwardAuth mechanism, and routes requests to the appropriate microservices.

Each microservice:
- has its own database
- can be deployed and scaled independently
- communicates asynchronously using RabbitMQ

Two RabbitMQ communication patterns are used:
1. **Fanout synchronization exchange** – for propagating structural events
2. **Load-balanced queues** – for distributing measurement data across monitoring replicas

---

## Microservice Synchronization (RabbitMQ – Fanout)

A fanout exchange named `sync.exchange` is used to propagate state changes between services.

Published events:
- `USER_CREATED`, `USER_DELETED` – published by `user-service`
- `DEVICE_CREATED`, `DEVICE_UPDATED`, `DEVICE_DELETED` – published by `device-service`
- `MEASUREMENT_CREATED` – published by the simulator

Queues:
- `sync.queue.device` – consumed by `device-service`
- `sync.queue.monitoring` – consumed by `monitoring-service`

All synchronization messages are transmitted as `SyncEvent` objects serialized with Jackson.

---

## Load Balancing for Measurements

To support horizontal scaling of the monitoring layer, a **Load Balancing Service** is introduced.

### Load Balancing Service

`load-balancing-service` consumes raw measurement messages from a single input queue:

- `device_data_queue`

For each message:
1. The `deviceId` is extracted from the payload
2. A deterministic hash (CRC32) is computed from `deviceId`
3. The message is routed to **one specific ingest queue**:
    - `ingest-1`, `ingest-2`, ..., `ingest-N`

This ensures:
- **No round-robin**
- All measurements of the same device always reach the same monitoring replica
- Correct aggregation of hourly consumption

---

## Monitoring Service (Replicated)

`monitoring-service` is deployed with multiple replicas (Docker Swarm).

Each replica:
- listens to exactly one ingest queue (`ingest-X`)
- processes measurements independently
- aggregates hourly consumption in its own database connection pool
- detects overconsumption events

### Overconsumption Detection

For each device:
- hourly energy is accumulated
- consumption is compared against the configured maximum power
- a cooldown mechanism prevents repeated notifications

When overconsumption is detected, a notification is sent to the WebSocket service.

---

## WebSocket Notifications

`websocket-service` exposes a STOMP/SockJS endpoint at:
/ws


Two types of messages are delivered:
- **Overconsumption notifications**
- **STOP commands** for simulators

Notifications are broadcast on platform-level topics (e.g. `/topic/notifications`), as required by the assignment specification.

> The assignment does not explicitly require user-specific filtering of notifications.  
> Notifications are therefore delivered at platform level, not per user.

---

## Energy Data Simulator

The simulator runs locally (outside Docker).

Responsibilities:
- periodically generates measurement data
- publishes `MEASUREMENT_CREATED` events to RabbitMQ
- connects to `websocket-service` to listen for STOP commands

The simulator stops automatically when a STOP command is received for its device.

---

## Frontend

The frontend is implemented in React and served via Nginx.

Main features:
- authentication and registration
- listing devices assigned to the logged-in user
- visualization of historical hourly consumption
- real-time overconsumption notifications via WebSocket

---

## Databases

Each microservice has its own PostgreSQL database:
- `auth_db`
- `user_db`
- `device_db`
- `monitoring_db`

Connection pooling is configured carefully to support multiple replicas of monitoring-service.

---

## Deployment

The system is deployed using Docker and Docker Swarm.

### Build backend services

mvn clean package

### Build Docker images
docker build -t auth-service .
docker build -t user-service .
docker build -t device-service .
docker build -t monitoring-service .
docker build -t load-balancing-service .
docker build -t websocket-service .
docker build -t customer-support-service .
docker build -t frontend .

### Deploy stack (Swarm)
docker stack deploy -c docker-stack.yml ems

### Access Points

Frontend: http://localhost

Traefik Dashboard: http://localhost:8080

RabbitMQ Management UI: http://localhost:15672

### Conclusion

This stage introduces true distributed processing by:

adding deterministic load balancing

supporting replicated monitoring services

enabling real-time notifications via WebSockets

The system remains modular, scalable, and compliant with the assignment requirements.