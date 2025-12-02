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

%%{init: {'themeVariables': { 'fontSize': '14px', 'fontFamily': 'arial'}, 'flowchart': {'nodeSpacing': 50, 'rankSpacing': 50}}}%%
graph LR
    %% --- 1. External Actors ---
    subgraph External [External Actors]
        direction TB
        User_Browser["User (Browser UI)"]
        Simulator_App["Device Data Simulator<br>(Local App)<br/>Publishes at amqp://localhost:5672"]
    end

    %% --- 2. Docker Host Environment ---
    subgraph DockerHost [Docker Host Environment]
        
        %% Infrastructure Layer
        subgraph infra_layer [Infrastructure & Routing Layer]
            direction TB
            traefik_node("Traefik Gateway<br/>HTTP Port: 80<br/>Dashboard: 8080")
            frontend_node("Frontend (Nginx)<br/>Exposed via Traefik '/'<br/>Container Port: 80")
            rabbitmq_node("RabbitMQ Broker<br/>AMQP: 5672<br/>Management UI: 15672")
        end

        %% Backend Microservices
        subgraph backend_layer [Backend Microservices]
            direction TB
            auth_node("Auth Service<br/>HTTP Port: 8083")
            user_node("User Service<br/>HTTP Port: 8081")
            device_node("Device Service<br/>HTTP Port: 8082")
            monitoring_node("Monitoring Service<br/>HTTP Port: 8084")
        end

        %% Database Layer
        subgraph data_layer [Database Layer]
            direction TB
            auth_db[("Auth DB (PostgreSQL)<br/>Port: 5432")]
            user_db[("User DB (PostgreSQL)<br/>Port: 5432")]
            device_db[("Device DB (PostgreSQL)<br/>Port: 5432")]
            monitoring_db[("Monitoring DB (PostgreSQL)<br/>Port: 5432")]
        end
    end

    %% ============= CONNECTIONS =============

    %% --- HTTP Routing (Traefik) ---
    User_Browser ===> |"HTTP :80"| traefik_node
    traefik_node --> |"Route '/' → Frontend"| frontend_node

    traefik_node --> |"Route /api/auth → 8083"| auth_node
    traefik_node --> |"Route /api/users → 8081"| user_node
    traefik_node --> |"Route /api/devices → 8082"| device_node
    traefik_node --> |"Route /api/monitoring → 8084"| monitoring_node

    %% --- Database Connections ---
    auth_node --> |"JDBC 5432"| auth_db
    user_node --> |"JDBC 5432"| user_db
    device_node --> |"JDBC 5432"| device_db
    monitoring_node --> |"JDBC 5432"| monitoring_db

    %% --- RabbitMQ Async Flows ---
    Simulator_App -.-> |"AMQP Publish :5672<br/>MEASUREMENT_CREATED"| rabbitmq_node
    rabbitmq_node -.-> |"Consume :5672<br/>MEASUREMENT_CREATED"| monitoring_node

    user_node -.-> |"Publish USER_CREATED / USER_DELETED"| rabbitmq_node
    device_node -.-> |"Publish DEVICE_CREATED / UPDATED / DELETED"| rabbitmq_node

    rabbitmq_node -.-> |"Consume USER_*"| device_node
    rabbitmq_node -.-> |"Consume DEVICE_*"| monitoring_node

    %% Styling
    classDef container fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    class DockerHost container;
    linkStyle default stroke-width:2px,fill:none,stroke:black;