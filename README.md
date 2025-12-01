Energy Management System – Part II
Microservices, RabbitMQ Synchronization, Historical Consumption in the Frontend

This project represents the second stage of the distributed system designed for managing users, IoT devices, and energy consumption monitoring. The architecture is organized into independent microservices, each with its own PostgreSQL database and communicating through HTTP and RabbitMQ. The frontend is implemented in React and communicates with the backend through Traefik, which acts as both a reverse proxy and an authentication gateway.

The system supports microservice synchronization, real-time processing of energy measurements sent by an external simulator, and visualization of hourly consumption in the client interface.

General Architecture

Traefik receives all incoming HTTP requests, validates the JWT token using the ForwardAuth mechanism, and routes the requests to the appropriate internal microservices. Each microservice maintains its own database to ensure data isolation and independent deployment.

Internal synchronization is implemented using RabbitMQ with a fanout exchange. The events transmitted are:

USER_CREATED, USER_DELETED – published by user-service and consumed by device-service

DEVICE_CREATED, DEVICE_UPDATED, DEVICE_DELETED – published by device-service and consumed by monitoring-service

MEASUREMENT_CREATED – published by the local simulator and consumed by monitoring-service

Energy consumption monitoring is performed in monitoring-service, which aggregates the measured values for each hour and stores them efficiently in its database.

The frontend supports user authentication, listing of devices assigned to the authenticated user, and visualization of daily energy consumption through charts.

Microservices
auth-service

Handles user registration, authentication, and JWT generation. Traefik uses the /auth/verify endpoint for token validation.
Associated database: auth_db

user-service

Exposes CRUD operations for users and publishes synchronization events to RabbitMQ.
Associated database: user_db

device-service

Manages IoT devices, user–device assignment and unassignment, and both publishes and consumes synchronization events.
Associated database: device_db

monitoring-service

Consumes device and measurement events, aggregates hourly consumption for each device, and exposes endpoints for historical visualization.
Associated database: monitoring_db

RabbitMQ and Microservice Synchronization

RabbitMQ is used to propagate state changes between microservices. A fanout exchange named sync.exchange is created, and each service has its own dedicated queue:

sync.queue.device – for device-service

sync.queue.monitoring – for monitoring-service

Structural updates are sent as SyncEvent objects serialized using Jackson.

Energy Data Simulator

The simulator runs outside Docker (locally) and periodically sends MEASUREMENT_CREATED events to RabbitMQ. These events are consumed by monitoring-service, which aggregates their values into the hourly_consumption table.

The simulator computes consumption based on the current hour and sends measurements at configurable intervals.

Frontend

The frontend is implemented in React and served through Nginx. Traefik exposes it at the root route (/).

Main functionalities:

authentication and registration

listing of devices assigned to the logged-in user

selecting a day from a calendar

visualizing hourly energy consumption using a bar or line chart

Running the System

Backend build:

mvn clean package


Docker image build:

docker-compose build


Start containers:

docker-compose up -d


Access endpoints:

Frontend: http://localhost

Traefik dashboard: http://localhost:8080
```mermaid
graph TB
    User["Browser User<br/>(React UI)"]

    Traefik["Traefik v3.1<br/>Reverse Proxy + ForwardAuth<br/>Ports: 80, 8080"]

    Frontend["Frontend (React + Nginx)<br/>Route: '/'<br/>Exposed via Traefik"]

    Auth["auth-service<br/>Port 8083<br/>Login/Register/JWT"]
    UserService["user-service<br/>Port 8081<br/>CRUD Users + Sync Publish"]
    DeviceService["device-service<br/>Port 8082<br/>CRUD Devices + Assignment + Sync Publish/Consume"]
    MonitoringService["monitoring-service<br/>Port 8084<br/>Hourly Aggregation + Rabbit Consumer"]

    AuthDB["auth-db (PostgreSQL)<br/>Port 5432"]
    UserDB["user-db (PostgreSQL)<br/>Port 5432"]
    DeviceDB["device-db (PostgreSQL)<br/>Port 5432"]
    MonitoringDB["monitoring-db (PostgreSQL)<br/>Port 5432"]

    Rabbit["RabbitMQ<br/>Ports 5672 / 15672<br/>sync.exchange (fanout)"]
    Simulator["Simulator (Local)<br/>Sends MEASUREMENT_CREATED"]

    User -->|"HTTP :80"| Traefik
    Traefik -->|"Route '/'"| Frontend

    Traefik -->|"/auth/*"| Auth
    Traefik -->|"/users/*"| UserService
    Traefik -->|"/devices/*"| DeviceService
    Traefik -->|"/monitoring/*"| MonitoringService

    Auth --> AuthDB
    UserService --> UserDB
    DeviceService --> DeviceDB
    MonitoringService --> MonitoringDB

    UserService -->|"Publish: USER_CREATED / USER_DELETED"| Rabbit
    DeviceService -->|"Publish: DEVICE_CREATED / UPDATED / DELETED"| Rabbit

    Simulator -->|"Publish: MEASUREMENT_CREATED"| Rabbit

    Rabbit -->|"Consume: USER_*"| DeviceService
    Rabbit -->|"Consume: DEVICE_*"| MonitoringService
    Rabbit -->|"Consume: MEASUREMENT_CREATED"| MonitoringService
