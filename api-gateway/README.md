# API Gateway

← [Back to the main README](../README.md)

📖 [Architecture Documentation](../docs/architecture.md)

## Overview

The API Gateway is the primary entry point of the Library Platform.

Built with Spring Cloud Gateway and WebFlux, it is responsible for routing client requests to the appropriate microservices while providing centralized security, 
error handling and observability capabilities.

## Responsibilities

- request routing
- JWT validation
- centralized security
- error handling
- CORS management
- distributed tracing propagation
- test environment support

## Routing

The API Gateway acts as the single entry point for all client requests and forwards them to the appropriate microservice.

Configured routes include:

| External Path | Target Service |
|---------------|----------------|
| `/users/**` | User Service |
| `/books/**` | Book Service |
| `/loans/**` | Loan Service |
| `/ws/notifications**` | Notification Service (WebSocket) |

Path prefixes are removed before forwarding requests to downstream services, allowing each service to expose its API independently.

The gateway also handles WebSocket routing for real-time notifications, forwarding client connections to the Notification Service.

```text
Client
   │
   ▼
API Gateway
   │
   ├── /users/**  → User Service
   ├── /books/**  → Book Service
   ├── /loans/**  → Loan Service
   └── /ws/notifications/** → Notification Service
```

## Security

The gateway acts as an OAuth2 Resource Server and validates JWT tokens issued by Keycloak.

Anonymous access is allowed only for a limited set of endpoints required by the platform, while all other routes require authentication.

## Error Handling

Custom handlers provide consistent responses for:

- authentication failures;
- authorization failures;
- downstream service errors;
- timeout scenarios;
- invalid requests.

## Observability

The gateway participates in distributed tracing and propagates trace information across downstream services.

Zipkin integration is enabled through Spring Boot tracing infrastructure.

## Test Support

When running with the `gherkin` profile, additional endpoints become available to support end-to-end testing scenarios.

These endpoints allow test suites to:

- reset platform state;
- clean test data;
- coordinate environment setup across services.

## Deployment

The service is containerized through Docker and is intended to be deployed as part of the platform Docker Compose environment.

A Dockerfile is provided to build the service image used by the development and testing environments.

## Technology Stack

- Java 21
- Spring Boot
- Spring Cloud Gateway
- Spring Security
- OAuth2 Resource Server
- WebFlux
- Zipkin
- Docker