Microservices-Based Energy Management System

This project implements a microservices architecture for managing users, authentication, and IoT devices. The system consists of separate services for authentication, user management, and device management, each running in its own Docker container and connected to a dedicated PostgreSQL database. The frontend is developed in React and communicates with the backend through Traefik, which acts as a reverse proxy and authentication gateway.

The backend is built using Spring Boot 4.0.0-SNAPSHOT, Java 25, and PostgreSQL. Each service exposes a REST API. Traefik handles routing and authentication forwarding between the frontend and backend services.

Services and Ports

auth-service: handles user registration, login, and JWT verification (port 8083)

user-service: manages user data and CRUD operations (port 8081)

device-service: manages IoT devices, assignment and unassignment to users (port 8082)

traefik: reverse proxy and load balancer (port 80 for HTTP, 8080 for dashboard)

frontend: React application served through Nginx (port 80)

PostgreSQL databases for each service (auth_db, user_db, device_db)

Docker containers communicate over a common network called ems. Each microservice connects to its respective database using environment variables defined in the .env file.

How to Run

Build all backend services using Maven: mvn clean package

Build Docker images for each service: docker-compose build

Start all containers: docker-compose up -d

Access the system:

Frontend: http://localhost

Traefik dashboard: http://localhost:8080

Architecture Overview
The frontend interacts with Traefik, which forwards requests to the corresponding backend service based on the request path. Authentication is managed through the auth-service, which issues JWT tokens. Traefik uses the /auth/verify endpoint for token validation through forward authentication. Each backend service communicates with its own PostgreSQL container for persistent storage.

Technologies Used

Spring Boot 4.0.0-SNAPSHOT

Java 25

PostgreSQL 18

React

Traefik v3.1

Docker and Docker Compose


This architecture ensures modularity, scalability, and clear separation of concerns between authentication, user management, and device management. Each service can be independently maintained and deployed while communicating securely through Traefik.

```mermaid
graph TD
    %% ======================
    %% 1. Actor
    %% ======================
    User_Browser["Utilizator (Browser)"]

    %% ======================
    %% 2. Docker Host + Containere
    %% ======================
    subgraph Docker_Host [Docker Host]
        direction LR

        %% --- TRAEFIK ---
        subgraph traefik_node [Container: traefik]
            traefik_artifact("Artifact: Traefik v3.0<br/>Reverse Proxy + ForwardAuth")
            traefik_ports("Ports Host 80:80, 8080:8080")
        end

        %% --- FRONTEND ---
        subgraph frontend_node [Container: frontend]
            frontend_artifact("Artifact: React + Nginx<br/>Serves UI + sends API calls")
            frontend_ports("Exposed via Traefik (Route '/')")
        end

        %% --- AUTH SERVICE ---
        subgraph auth_node [Container: auth-service]
            auth_artifact("Artifact: auth-service.jar<br/>Spring Boot + Tomcat")
            auth_ports("Port Host 8083")
        end

        %% --- USER SERVICE ---
        subgraph user_node [Container: user-service]
            user_artifact("Artifact: user-service.jar<br/>Spring Boot + Tomcat")
            user_ports("Port Host 8081")
        end

        %% --- DEVICE SERVICE ---
        subgraph device_node [Container: device-service]
            device_artifact("Artifact: device-service.jar<br/>Spring Boot + Tomcat")
            device_ports("Port Host 8082")
        end

        %% --- AUTH DATABASE ---
        subgraph auth_db [Container: auth-db]
            auth_db_artifact("Artifact: PostgreSQL<br/>Credential Database")
            auth_db_ports("Port Host 5432")
        end

        %% --- USER DATABASE ---
        subgraph user_db [Container: user-db]
            user_db_artifact("Artifact: PostgreSQL<br/>User Database")
            user_db_ports("Port Host 5433")
        end

        %% --- DEVICE DATABASE ---
        subgraph device_db [Container: device-db]
            device_db_artifact("Artifact: PostgreSQL<br/>Device Database")
            device_db_ports("Port Host 5434")
        end
    end

    %% ======================
    %% 3. Conexiuni
    %% ======================

    %% --- Extern ---
    User_Browser -- "HTTP (Port 80)" --> traefik_node
    traefik_node -- "Route '/' → Frontend (React UI)" --> frontend_node

    %% --- Frontend → Servicii prin Traefik ---
    frontend_node -- "HTTP API Calls (fetch /auth, /users, /devices)" --> traefik_node
    traefik_node -- "/auth/*" --> auth_node
    traefik_node -- "/users/*" --> user_node
    traefik_node -- "/devices/*" --> device_node

    %% --- Servicii → Baze de date ---
    auth_node -- "JDBC (5432)" --> auth_db
    user_node -- "JDBC (5433)" --> user_db
    device_node -- "JDBC (5434)" --> device_db

 