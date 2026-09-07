# Event Contracts

← [Back to the main README](../README.md)

📖 [Architecture Documentation](../docs/architecture.md)

## Overview

The Event Contracts module defines the integration contracts used by the Library Platform microservices.

It provides versioned JSON Schemas, sample payloads and validation utilities used to ensure consistent communication between services through RabbitMQ.

The module acts as a central source of truth for all integration events exchanged within the platform.

## Purpose

In a distributed architecture, services communicate through asynchronous messages rather than direct method calls.

This module provides a formal contract for those messages and helps guarantee that:

- producers publish valid events;
- consumers receive expected payloads;
- schema evolution remains controlled;
- backward compatibility can be validated;
- event versioning is explicitly managed.

## Architectural Role

The module sits between event producers and consumers.

```text
Service A
    │
    │ Integration Event
    ▼
 Event Contract
    │
    ▼
RabbitMQ
    │
    ▼
 Service B
```

Contracts are intentionally separated from service implementations to allow event validation without creating runtime dependencies between microservices.

Contracts are shared by:

- Book Service
- Loan Service
- User Service
- Notification Service

and are used throughout the platform's event-driven architecture.

## Event Contract Structure

Each contract contains:

- JSON Schema definitions;
- sample payloads;
- version identifiers;
- event metadata requirements.

Contracts follow a consistent directory structure:

```text
contracts/
├── book/
├── bookRequest/
├── loan/
├── user/
└── common/
```

Each event may contain multiple schema versions:

```text
book.registered/
└── v1/

loan.returned/
├── v1/
└── v2/
```

## Event Envelope

All integration events extend a shared event envelope.

The envelope provides metadata required for event processing and tracing:

- eventId
- aggregateId
- eventVersion
- occurredAt
- schemaVersion

This common structure ensures consistency across all published events.

## Available Integration Events

The module currently defines integration event contracts for the following services.

### Book Service Integration Events

- BOOK_REGISTERED
- BOOK_COPIES_UPDATED
- BOOK_BORROWED
- BOOK_RELEASED
- BOOK_RESERVED
- BOOK_RETURNED
- BOOK_BORROW_REJECTED
- BOOK_RESERVATION_REJECTED

### Book Request Integration Events

- BOOK_REQUEST_ADDED
- BOOK_REQUEST_APPROVED
- BOOK_REQUEST_REJECTED
- BOOK_REQUEST_VOTED
- BOOK_REQUEST_PRICE_UPDATED

### Loan Service Integration Events

- LOAN_REQUESTED
- LOAN_RESERVED
- LOAN_CONFIRM_REQUESTED
- LOAN_CONFIRMED
- LOAN_CANCELED
- LOAN_RETURNED
- LOAN_FAILED

### User Service Integration Events

- USER_SUBSCRIBED
- USER_UNSUBSCRIBED
- USER_SUSPENDED
- USER_UNSUSPENDED
- LIBRARY_CARD_ASSIGNED

## Versioning Strategy

Contracts support explicit versioning through schema directories.

Examples include:

- USER_SUBSCRIBED v1 and v2
- LOAN_RETURNED v1 and v2

This approach allows event schemas to evolve while maintaining compatibility with existing consumers.

Contract versioning allows new business requirements to be introduced without forcing all consumers to migrate simultaneously.

## Contract Validation

The module includes JSON Schema validation utilities based on the JSON Schema Draft 2020-12 specification.

Contract validation is used by producer and consumer tests to ensure:

- published events conform to their schemas;
- sample payloads remain valid;
- breaking changes are detected early;
- event evolution is controlled.

## Testing Support

The contracts are consumed by:

- producer contract tests;
- consumer contract tests;
- integration tests.

This allows services to validate event compatibility independently of the runtime environment.

## Technology Stack

- Java 21
- JSON Schema Draft 2020-12
- json-schema-validator