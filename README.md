# Library Platform

Distributed Spring Boot Microservices Architecture

## Overview

This repository represents the third iteration of a Library Management System developed throughout a software architecture mentoring journey.

The previous monolithic implementation successfully consolidated Domain-Driven Design, Event Sourcing, and CQRS-inspired architectural concepts. 
However, it also revealed a new set of challenges involving scalability, service boundaries, event publication reliability and operational concerns.

To explore these topics, the application was redesigned as a distributed system composed of multiple Spring Boot microservices communicating through 
asynchronous events.

The primary objectives of this phase were:

- Distributed Architecture
- Event-Driven Communication
- Event Sourcing
- Reliable Event Publication
- Service Autonomy
- Security and Identity Management
- Observability
- End-to-End Testing

The resulting platform provides a complete library management solution while serving as an opportunity to explore many of the challenges commonly 
encountered in enterprise microservice ecosystems.

## Mentoring Journey

This project was developed as part of a structured mentoring program focused on software architecture and backend engineering.

Throughout the mentoring journey, architectural decisions were regularly reviewed and discussed with my mentor.

While the first iterations focused primarily on understanding and consolidating architectural patterns, this implementation explores 
how those concepts behave when applied to a distributed environment.

Particular attention was given to:

- service decomposition;
- bounded contexts;
- asynchronous communication;
- infrastructure concerns;
- reliability patterns;
- operational observability;
- automated testing strategies.

## Learning Journey

This repository represents the third phase of a multi-stage evolution of the same business domain.

| Phase	| Technology	| Main Focus |
|---------|---------|---------|
| Phase 1 |	Node.js + TypeScript | Learning a new ecosystem while exploring DDD and Event Sourcing |
| Phase 2 |	Java + Spring Boot Monolith	| Consolidating architectural concepts in a familiar ecosystem |
| Phase 3 |	Java + Spring Boot Microservices | Applying the lessons learned to a distributed architecture |
| Phase 4 |	Angular Frontend | Demonstrating the microservices platform through a web interface |

This implementation represents the natural evolution of the concepts introduced in the previous repositories.

## Architecture Concepts

The platform explores a variety of architectural patterns and practices:

- Domain-Driven Design (DDD)
- Event Sourcing
- CQRS-inspired read models
- Microservices Architecture
- Event-Driven Communication
- Outbox Pattern
- Event Replay
- API Gateway Pattern
- Contract-Driven Integration
- Role-Based Access Control (RBAC)
- Distributed Tracing

The architecture is composed of multiple independently deployable services.

```text
Client
   │
   ▼
API Gateway
   │
   ├── Book Service
   ├── Loan Service
   ├── User Service
   └── Notification Service
```

```text
Book / Loan / User Services
            │
            ▼
        RabbitMQ
            │
            ▼
 Notification Service
```

```text
Each Service
├── Domain
├── Application
├── Infrastructure
├── Event Store
├── Outbox
└── Projections
```

## Documentation

This repository contains additional documentation describing the architecture and infrastructure of the platform:

- [Architecture](./docs/architecture.md)
- [Infrastructure](./docker/README.md)

## Platform Modules

| Module | Responsibility |
|----------|----------|
| [API Gateway](./api-gateway/README.md) | Entry point, routing and security |
| [Book Service](./book-service/README.md) | Catalogue, availability, subscriptions and purchase requests |
| [Loan Service](./loan-service/README.md) | Loan lifecycle and statistics |
| [User Service](./user-service/README.md) | User management and identity integration |
| [Notification Service](./notification-service/README.md) | Event-driven notifications |
| [Shared Core Library](./shared-core-library/README.md) | Shared event contracts, DTOs, and cross-service abstractions |
| [Shared JPA Library](./shared-jpa-library/README.md) | Reusable persistence abstractions and event-store infrastructure |
| [Library Tests](./library-tests/README.md) | End-to-end testing infrastructure, Testcontainers and BDD Gherkin scenarios |
| [Event contracts](./contracts/README.md) | Event schemas and contracts used to ensure consistency between services |

## Event-Driven Architecture

Services communicate primarily through asynchronous events using RabbitMQ.

Key architectural capabilities include:

- reliable event publication through the Outbox Pattern;
- service decoupling;
- eventual consistency;
- event replay support;
- projection rebuilding;
- dead-letter queues (DLQ);
- contract-driven event integration.

Unlike the monolithic implementation, events are no longer dispatched synchronously within the same process.

This version introduces a dedicated event distribution infrastructure designed to support long-term scalability and reliability.

## Event Replay

The platform supports replaying domain events in order to rebuild projections and restore read models.

Because the platform follows an Event Sourcing approach, projections are treated as derived views rather than the primary source of truth. 
Whenever a projection becomes inconsistent, when its implementation changes, or when a new projection is introduced, historical events can be replayed to rebuild the 
current state.

Event replay can be used for:

- rebuilding corrupted projections;
- introducing new read models;
- validating projection consistency;
- recovering from projection failures;
- applying projection changes without modifying historical data.

Replay processing is implemented through dedicated RabbitMQ infrastructure and event-processing pipelines. 
During a replay, historical events are reprocessed by the target service and projections are rebuilt from the persisted event streams.

### Executing a Replay

The repository provides a PowerShell script that publishes replay requests for all event-sourced services:

```powershell
replay.ps1
```

The script publishes replay messages for:

- Book Service
- Loan Service
- User Service

The replay requests are routed through the dedicated replay exchange and consumed by the corresponding services, which rebuild their projections from 
historical events.

Although the provided script offers the simplest way to execute a replay, replay requests can also be published manually through the messaging infrastructure.

### Requirements

Before executing a replay:

- RabbitMQ must be running;
- the target services must be running;
- the platform must already be started using the development environment.

A successful replay does not modify historical events. Instead, it reconstructs projections by reprocessing the existing event streams stored by each service.

## Security

The platform integrates Keycloak as a centralized identity and access management solution.

Security capabilities include:

- JWT-based authentication
- Role-Based Access Control (RBAC)
- Centralized identity management through Keycloak
- End-to-end JWT validation across services
- User and role propagation across the platform

Roles include:

- READER
- LIBRARIAN
- ADMIN

### Authentication Flow

Authentication is centralized through Keycloak.

JWT tokens issued by Keycloak are validated by the API Gateway and subsequently propagated to downstream services. 
Individual services validate incoming tokens and extract user context and roles directly from JWT claims.

```text
Client
   │
   │ JWT issued by Keycloak
   ▼
API Gateway
   │
   │ validates JWT
   ▼
Microservices
   │
   │ validate JWT
   │ extract username and roles
   ▼
Business Logic
```

The authenticated username is propagated through the `preferred_username claim`, while authorization roles are obtained from `realm_access.roles`.

## Observability

The platform includes distributed tracing support using Zipkin.

This allows requests and events to be tracked across multiple services and provides greater visibility into distributed workflows.

## Testing

The platform adopts a multi-layered testing strategy.

### Service-Level Tests

Each microservice includes its own JUnit-based test suite covering:

- domain logic;
- application services;
- projections;
- event handlers;
- infrastructure components.

These tests can be executed directly from the IDE or through Maven.

### End-to-End Tests

The repository includes a dedicated `library-e2e-tests` module containing Behavior-Driven Development (BDD) scenarios written in Gherkin.

The purpose of these tests is to validate complete business workflows across the entire distributed platform rather than individual service behavior.

The end-to-end scenarios exercise interactions between:

- API Gateway;
- Book Service;
- Loan Service;
- User Service;
- Notification Service;
- RabbitMQ;
- Keycloak;
- MySQL databases.

Business processes, event propagation, security integration and cross-service consistency are validated through executable specifications.

## Running the Tests

### Requirements

All test suites rely on Docker-based infrastructure.

Ensure Docker is installed and running before executing any test.

The project uses:

- Testcontainers for service-level integration tests;
- Docker Compose environments for end-to-end testing.

The platform provides two complementary testing approaches.

### Service-Level Tests

Each microservice contains its own JUnit test suite.

Tests can be executed:

- directly from the IDE;
- through Maven

```bash
mvn test
```

### End-to-End Tests

The `library-e2e-tests` module contains BDD scenarios implemented with Cucumber and Gherkin.

Before executing the suite, start the dedicated testing environment:

```bash
start-gherkin.bat
```

Once the environment is available, execute the tests from the IDE by running the JUnit test suite contained in the `library-e2e-tests` project.

The end-to-end scenarios provide automated validation of the complete distributed architecture.

## Lessons Learned

This implementation highlighted several challenges that were not visible in the monolithic architecture.

The most important lessons involved:

- reliable event publication;
- distributed consistency;
- service autonomy;
- security boundaries;
- operational visibility;
- event versioning concerns;
- infrastructure automation.

The introduction of patterns such as the Outbox Pattern, Event Replay and centralized identity management significantly improved the robustness of the platform.

At the same time, the project exposed additional areas for future exploration, including advanced resilience patterns and production-grade operational monitoring.

## Why This Repository Exists

While the business domain chosen for this project is a library management platform, the primary goal of the repository is to explore and demonstrate architectural 
patterns that can be applied across a wide range of enterprise systems.

The challenges addressed by this solution are not specific to libraries. Similar requirements can be found in many business domains, including:

- e-commerce platforms;
- booking and reservation systems;
- logistics and supply-chain applications;
- healthcare systems;
- financial platforms;
- customer management solutions.

The repository focuses on architectural concerns commonly encountered in modern distributed systems, such as:

- service decomposition;
- event-driven communication;
- reliable event publication;
- distributed security;
- observability;
- projection rebuilding and event replay;
- end-to-end testing of integrated environments.

Beyond the implementation itself, the project aims to document architectural decisions, trade-offs and lessons learned while evolving a business application from 
a monolithic architecture to a distributed microservices platform.

For this reason, the repository should be viewed not only as a library management application, but also as a reference implementation of modern backend and 
software architecture practices.

Particular attention has been given to making architectural decisions explicit, with the goal of making the project useful both as a software solution and 
as a learning resource for developers interested in modern backend and distributed-system architecture.

## Running the Platform

### Requirements

- Java 21
- Maven
- Docker
- Docker Compose

Docker must be installed and running before starting the platform.

### Development Environment

#### Using the Helper Script (Windows)

The easiest way to start the platform is by using the provided helper script:

```bat
start-dev.bat
```

The script automatically:

- builds all Maven modules;
- creates the required Docker images;
- starts the development environment;
- provisions the required infrastructure and services.

#### Manual Startup

If you prefer not to use the helper script, the platform must first be built and all Docker images must be created.

Build the project:

```bash
mvn clean install
```

Build the service images, from the root project directory:

```bash
docker build -t api-gateway:dev ./api-gateway
 
docker build -t book-service:dev ./book-service
 
docker build -t loan-service:dev ./loan-service
 
docker build -t user-service:dev ./user-service
 
docker build -t notification-service:dev ./notification-service
```

Once the images have been created, move to the Docker directory:

```bash
cd docker
```

and start the development environment:

```bash
docker compose -p dev --profile dev up -d
```

#### Available Services

The development environment provisions:

- API Gateway
- Book Service
- Loan Service
- User Service
- Notification Service
- RabbitMQ
- Keycloak
- MySQL databases
- Zipkin

Once the environment has started successfully, the following services are available:

| Component | URL |
|------------|------|
| API Gateway | http://localhost:8080 |
| Keycloak | http://localhost:8084 |
| RabbitMQ Management | http://localhost:15672 |
| Zipkin | http://localhost:9411 |

In development mode the individual microservices are also directly accessible through their exposed ports for debugging and troubleshooting purposes.

In a production deployment, only the API Gateway would normally be exposed externally, while business services would remain accessible only through the internal 
network.

### Test Environment

A dedicated environment is available for end-to-end testing.

#### Using the Helper Script (Windows)

The testing environment can be started using:

```bat
start-gherkin.bat
```

The script rebuilds the platform, recreates the testing infrastructure and starts the complete environment used by the end-to-end test suite.

#### Manual Startup

After building the application and Docker images, change to the Docker directory:

```bash
cd docker
```

and start the dedicated testing environment:

```bash
docker compose -p gherkin --profile gherkin up -d
```

#### Available Services

The test environment provisions isolated infrastructure used by the `library-e2e-tests` module:

- API Gateway
- Book Service
- Loan Service
- User Service
- Notification Service
- RabbitMQ
- Keycloak
- MySQL databases
- Zipkin

Once the environment has started successfully, the following services are available:

| Component | URL |
|------------|------|
| API Gateway | http://localhost:9080 |
| Keycloak | http://localhost:8084 |
| RabbitMQ Management | http://localhost:15673 |
| Zipkin | http://localhost:9412 |

## Development Credentials

The repository includes development credentials, RabbitMQ definitions and Keycloak realm exports required to bootstrap the local environment. 
These credentials are intended exclusively for local development and demonstration purposes and do not provide access to any external systems.

## License

See the `LICENSE` file for licensing information.