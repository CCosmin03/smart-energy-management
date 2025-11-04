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
%% ===== STYLING =====
classDef docker fill:#EAF3FF,stroke:#1E56A0,stroke-width:2px,color:#000,font-size:12px
classDef db fill:#D6EAF8,stroke:#2980B9,stroke-width:2px,color:#000,font-size:12px
classDef actor fill:#FFF3E0,stroke:#E67E22,stroke-width:2px,color:#000,font-size:12px

%% ===== ACTOR =====
A[/"End User\n(Client / Admin)"/]:::actor

%% ===== FRONTEND =====
B[Docker: frontend\nReact + Nginx\nPorts 80:80]:::docker
A -->|HTTP (80)| T

%% ===== TRAEFIK =====
T[Docker: traefik\nReverse Proxy + Auth Forwarding\nPorts 80:80, 8080:8080]:::docker
T -->|Route / → frontend| B
T -->|/auth/*| C
T -->|/users/*| D
T -->|/devices/*| E

%% ===== AUTH SERVICE =====
C[Docker: auth-service\nSpring Boot + Tomcat\nPort 8083]:::docker
C -->|JDBC (5432)| F
F[Docker: auth-db\nPostgreSQL\nPort 5432]:::db

%% ===== USER SERVICE =====
D[Docker: user-service\nSpring Boot + Tomcat\nPort 8081]:::docker
D -->|JDBC (5433)| G
G[Docker: user-db\nPostgreSQL\nPort 5433]:::db

%% ===== DEVICE SERVICE =====
E[Docker: device-service\nSpring Boot + Tomcat\nPort 8082]:::docker
E -->|JDBC (5434)| H
H[Docker: device-db\nPostgreSQL\nPort 5434]:::db

%% ===== INTER-SERVICE COMMUNICATION =====
C -->|POST /users (register)| D
D -->|GET /devices/user/{id}| E
E -->|GET /users/{id}| D
