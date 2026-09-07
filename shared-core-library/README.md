# Shared Core Library

← [Back to the main README](../README.md)

📖 [Architecture Documentation](../docs/architecture.md)

## Overview

The Shared Core Library provides the foundational building blocks used across the Library Platform microservices.

Rather than duplicating common infrastructure and integration logic inside each service, the platform centralizes shared concepts such as event processing, 
messaging abstractions, replay infrastructure, outbox contracts and common domain models in a dedicated library.

The module promotes consistency across services while reducing duplication and simplifying maintenance.

## Purpose

The library provides reusable components for:

- event-driven communication;
- Event Sourcing infrastructure;
- Replay infrastructure;
- Outbox abstractions;
- integration event handling;
- notification event handling;
- shared domain models;
- messaging topology definitions;
- payload validation.

## Architectural Role

The module acts as a common foundation for all application services.

The library is used by:

- API Gateway
- Book Service
- Loan Service
- User Service
- Notification Service

and provides the common infrastructure required by the platform's event-driven architecture.

The library contains no business-specific logic and focuses exclusively on reusable platform capabilities.

## Shared Infrastructure

### Event Processing

The library provides a shared framework for processing integration events.

Core components include:

- IntegrationEventEnvelope
- IntegrationEventTypes
- EventHandler
- EventHandlerRegistry
- HandlerMetadata
- AbstractEventHandler

These abstractions provide consistent event routing, payload validation and version-aware handler resolution across all services.

Handlers are registered through metadata annotations and resolved based on:

- event type;
- schema version.

This approach supports safe event evolution and simplifies the introduction of new event versions while maintaining backward compatibility.

#### Versioning Support

The library provides built-in support for event versioning.

Handlers can declare the schema versions they support through metadata annotations, allowing multiple versions of the same event to coexist safely.

This approach supports contract evolution while maintaining backward compatibility across services.

### Notifications

The library provides dedicated abstractions for notification processing.

Key components include:

- NotificationEventEnvelope
- NotificationEventType
- NotificationHandler
- NotificationHandlerRegistry
- NotificationHandlerMetadata

This infrastructure enables version-aware notification processing while keeping notification consumers independent from implementation details.

### Replay

The platform supports rebuilding projections through replay.

The shared replay infrastructure is based on:

- AbstractReplayService
- ReplayProjection
- ReplayEventHandlerRegistry
- ReplayRequestedEvent

The replay framework provides:

- temporary read model creation;
- event streaming;
- projection rebuilding;
- atomic read model replacement;
- automatic cleanup on failure.

This allows services to rebuild projections safely from historical events stored in their event stores.

### Outbox

The library defines the contracts used by the Outbox Pattern implementation.

Key abstractions include:

- OutboxEvent
- OutboxRepository
- OutboxStatus

These contracts provide a common model used by services to guarantee reliable event publication.

### Messaging Topology

The library centralizes messaging infrastructure definitions.

Shared constants include:

- exchanges;
- queues;
- replay queues;
- notification queues;
- routing key mappings.

This avoids duplicated messaging configuration across services and provides a single source of truth for RabbitMQ topology definitions.

## Integration Events

The library defines the shared catalog of integration event types exchanged throughout the platform.

Supported events include:

- Book Events
- Loan Events
- User Events
- Book Request Events

The complete event definitions and schemas are documented in the Event Contracts module.

## Shared Domain Models

The library provides common domain models used across multiple services.

Examples include:

- UserRole
- UserStatus
- TokenPrincipal
- ErrorResponse

These models promote consistency throughout the platform and reduce duplication of shared concepts.

## Validation Support

The library includes reusable payload validation utilities.

Incoming event payloads are:

- mapped to strongly-typed objects;
- validated through Jakarta Validation;
- rejected if validation fails.

This ensures that handlers only process valid event payloads.

## Spring Boot Integration

The module provides auto-configuration support through the `SharedLibraryCoreAutoConfiguration` class.

This configuration automatically registers:

- payload mapping;
- event handler registries;
- notification handler registries.

Services can therefore reuse the infrastructure with minimal setup.

## Testing

The library includes tests covering:

- event handler registration;
- replay infrastructure;
- handler version resolution;
- duplicate handler detection;
- payload processing workflows.

These tests help ensure platform-wide consistency and reliability.

## Technology Stack

- Java 21
- Spring Boot
- Jakarta Validation
- JUnit 5
- Mockito