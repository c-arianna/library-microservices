# Architecture Documentation

## Introduction

The Library Platform is a distributed microservices-based application built to explore and demonstrate architectural patterns commonly used in modern enterprise 
systems.

The project represents the latest stage of a multi-phase software architecture learning journey, evolving from earlier monolithic implementations toward a fully distributed architecture.

Although the business domain focuses on library management, the architectural approaches implemented throughout the platform are applicable to a wide range of 
domains, including:

- e-commerce;
- booking systems;
- healthcare applications;
- logistics platforms;
- customer management systems;
- financial applications.

The primary goal of the platform is not the business domain itself, but the exploration of architectural concerns such as service autonomy, event-driven 
communication, security, observability, reliability, and projection rebuilding.

---

## Architecture Overview

The platform consists of three business services, a dedicated Notification Service and an API Gateway.

The supporting infrastructure includes RabbitMQ, Keycloak and Zipkin.

```text
               ┌──────────────┐
               │   Frontend   │
               └──────┬───────┘
                      │
                      ▼
               ┌───────────────┐
               │  API Gateway  │
               └──────┬────────┘
                      │
				      ▼
      ┌──────────┬──────────┬────────────┐
      ▼          ▼          ▼            ▼
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────┐
│  Book   │ │  Loan   │ │  User   │ │ Notification│
│ Service │ │ Service │ │ Service │ │   Service   │
└────┬────┘ └────┬────┘ └────┬────┘ └──────┬──────┘
     │           │           │             │
     └───────────┴───────────┴─────────────┘
                    RabbitMQ
```

The API Gateway acts as the single entry point for all client applications and centralizes security, routing and request processing concerns.

Business capabilities are distributed across three autonomous bounded contexts:

- Book Service, responsible for catalogue management and purchase workflows;
- Loan Service, responsible for loan lifecycle management;
- User Service, responsible for user management and identity-related concerns.

The services communicate primarily through asynchronous integration events exchanged through RabbitMQ.

Each service owns its own persistence model, event store, projections and business logic, ensuring a high degree of autonomy and loose coupling.

The Notification Service is isolated from business domains and focuses exclusively on user communication concerns. 
It consumes notification events from RabbitMQ and delivers updates through WebSocket and SMS channels.

This architecture allows services to evolve independently while maintaining consistency through event-driven integration patterns.

---

## Service Boundaries

The platform is divided into a set of autonomous services, each responsible for a specific bounded context and owning its own persistence model, business rules 
and integration contracts.

### API Gateway

The API Gateway acts as the single entry point for client applications and centralizes cross-cutting concerns shared across the platform.

Responsibilities include:

- request routing;
- JWT validation;
- centralized error handling;
- CORS management;
- distributed tracing propagation;
- WebSocket routing.

The gateway does not contain business-domain logic and is responsible exclusively for request processing and security-related concerns.

### Book Service

The Book Service is the system of record for library catalogue management.

It owns all catalogue-related business rules and is responsible for maintaining the authoritative state of books, copies, subscriptions and purchase requests.

Responsibilities include:

- catalogue management;
- copy availability management;
- reservations;
- book subscriptions;
- purchase requests;
- purchase request voting;
- purchase request approval workflows;
- budget-constrained purchase recommendations.

### Loan Service

The Loan Service manages the complete lifecycle of loans.

It owns all loan-related business workflows and maintains the authoritative state of loan transactions across the platform.

Responsibilities include:

- loan requests;
- reservations;
- confirmations;
- returns;
- cancellations;
- loan statistics;
- dashboard data.

### User Service

The User Service manages users and account lifecycles.

It acts as the authoritative source for user identities, roles and library membership information.

Responsibilities include:

- user registration;
- role assignment;
- library card assignment;
- account suspension;
- account reactivation;
- identity provider synchronization.

### Notification Service

The Notification Service provides user-facing communication channels.

It does not contain business-domain logic and is responsible exclusively for delivering notifications generated by other services.

Responsibilities include:

- WebSocket notifications;
- user-specific notifications;
- SMS notifications;
- event-driven notification processing.

---

## Request Routing

Incoming requests are routed according to path-based routing rules configured in Spring Cloud Gateway.

```text
Client
   │
   ▼
API Gateway
   │
   ├── /books/**  ─────────► Book Service
   ├── /loans/**  ─────────► Loan Service
   ├── /users/**  ─────────► User Service
   └── /ws/notifications/** ───► Notification Service
```

The following routing rules are configured:

| Route | Target Service |
|---------|---------|
| `/books/**` | Book Service |
| `/loans/**` | Loan Service |
| `/users/**` | User Service |
| `/ws/notifications/**` | Notification Service |

For REST endpoints, route-specific filters remove the external path prefix before forwarding requests to downstream services.

As a result, internal services remain independent from the public URL structure exposed by the platform.

The gateway also routes WebSocket traffic to the Notification Service, enabling real-time notifications without exposing internal service endpoints to clients.

This approach centralizes request routing and prevents client applications from communicating directly with internal platform services.

---

## Security Architecture

Authentication and authorization are centralized through Keycloak.

The platform adopts a JWT-based security model in which both the API Gateway and the business services act as OAuth2 Resource Servers.

### Authentication Flow

Client authentication is delegated to Keycloak.

When a user signs in, Keycloak authenticates the user and issues a JWT access token. 
The token is returned to the client and included in subsequent authenticated requests.

```text
Client
   │
   │ Login
   ▼
Keycloak
   │
   │ JWT Access Token
   ▼
Client
   │
   │ Authorization: Bearer <token>
   ▼
API Gateway
   │
   │ JWT validation
   ▼
Target Service
   │
   │ JWT validation
   │ Extract username and roles
   ▼
Business Logic
```

The API Gateway validates JWT tokens before forwarding requests to downstream services.

Each service independently validates the received token and extracts the authenticated user and assigned roles from JWT claims.

This approach ensures that services remain independently deployable and independently secure, without relying on the gateway for authorization decisions.

### User Identity and Roles

User identity is obtained from the `preferred_username` claim.

Application roles are extracted from the `realm_access.roles` claim and mapped to Spring Security authorities.

### Supported Roles

The platform currently supports three application roles:

- READER;
- LIBRARIAN;
- ADMIN.

Authorization decisions are enforced by individual services according to their own business rules and access requirements.

### Identity Management

Authentication and authorization are centralized through Keycloak.

The platform consists of multiple independently deployable services exposing protected APIs and WebSocket endpoints.

Using a dedicated identity provider avoids replicating authentication logic across services and provides a centralized mechanism for managing users, roles and 
security policies.

Keycloak was selected because it provides:

- centralized identity management;
- OAuth2 and OpenID Connect support;
- JWT-based authentication;
- role-based access control;
- integration with Spring Security Resource Servers.

This approach allows business services to focus on domain responsibilities while delegating identity management concerns to a dedicated security component.

### WebSocket Security

The Notification Service exposes WebSocket endpoints used for real-time notifications.

WebSocket subscriptions are protected through Spring Security and role-based authorization rules. 
Access to notification channels is granted according to the authenticated user's roles and permissions.

This ensures that real-time notifications follow the same security model used by the REST APIs.

---

## Event-Driven Architecture

Services communicate primarily through asynchronous integration events exchanged through RabbitMQ.

Rather than relying on synchronous service-to-service communication, business services publish events that are consumed by interested services.

This approach promotes:

- loose coupling;
- service autonomy;
- independent deployment;
- eventual consistency;
- scalability.

```text
	Book Service
		│
		▼
	Domain Event
		│
		▼
	 Outbox
		│
		▼
  Integration Event
		│
		▼
	RabbitMQ
		│
   ┌────┴────┐
   ▼         ▼
Loan     Notification
Service    Service
```

Domain events produced by aggregates are transformed into versioned integration events and distributed through RabbitMQ.

Reliable publication is implemented through the Outbox Pattern, while consumers process integration events independently according to their own business requirements.

As a result, services can evolve independently while maintaining consistency through event-driven integration patterns.

---

## Domain Events vs Integration Events

The platform makes a clear distinction between domain events and integration events.

Although both represent business occurrences, they serve different purposes and evolve according to different requirements.

```text
Aggregate
    │
    ▼
Domain Event
    │
    ├── Event Store
    │
    └── Integration Event
              │
              ▼
           RabbitMQ
              │
              ▼
       Consumer Service
```

### Domain Events

Domain events represent business changes that occur within a single bounded context.

Examples include:

- BookRegisteredEvent
- LoanRequestedEvent
- UserSubscribedEvent

Domain events are:

- generated by aggregates;
- persisted in the event store;
- used to rebuild aggregate state;
- transformed into integration events used for projection updates and inter-service communication;
- internal to the originating service.

They are part of the domain model and are never exchanged directly between services.

### Integration Events

Integration events represent versioned contracts exchanged between services.

Examples include:

- BOOK_REGISTERED
- LOAN_REQUESTED
- USER_SUBSCRIBED

Integration events are:

- generated from domain events;
- published through RabbitMQ;
- versioned independently;
- governed by JSON Schema contracts;
- consumed by projections and external services.

They provide a stable communication layer between bounded contexts and are designed for long-term compatibility.

### Why Separate Them?

Separating domain events from integration events allows the internal domain model to evolve independently from inter-service contracts.

As a result:

- services can refactor their internal implementation without affecting consumers;
- integration contracts can evolve through versioning strategies;
- backward compatibility can be maintained across services;
- bounded contexts remain loosely coupled.

This separation is particularly important in an Event Sourcing architecture, where domain events belong to the internal lifecycle of a service, while integration 
events represent the public contracts shared with the rest of the platform.

---

## Event Contracts and Versioning

All integration events are governed by versioned JSON Schemas.

Examples include:

- USER_SUBSCRIBED v1
- USER_SUBSCRIBED v2
- LOAN_RETURNED v1
- LOAN_RETURNED v2

Versioning allows new business requirements to be introduced without breaking existing consumers.

The platform adopts different evolution strategies depending on the nature of the schema change and its impact on historical data and downstream consumers.

### Strategy 1: Migration Through a Technical Event

The introduction of the `cardNumber` field in the `USER_SUBSCRIBED` event required a migration strategy because the new information did not exist for users 
created before library card management was introduced.

The original event schema did not include library card information:

```text
USER_SUBSCRIBED v1
```

A new version was introduced with the additional field:

```text
USER_SUBSCRIBED v1
        │
        ▼
USER_SUBSCRIBED v2
      + cardNumber
```

The v2 event contains the same information as v1 together with the additional `cardNumber` field.

Event handler for USER_SUBSCRIBED supports both schema versions. For v2, validation rules ensure that reader users always provide a valid library card number.

Although this approach preserves compatibility for newly generated events, it does not solve the problem of users created before the introduction of library cards. 

Existing projections still lack the newly required information.

To address this scenario, a dedicated migration process was implemented in the User Service.

The migration is executed once through a dedicated Spring profile and performs the following steps:

1. retrieves users without a library card number;
2. generates a card number for each user;
3. updates the corresponding aggregate;
4. generates a domain event `LibraryCardAssignedEvent`.

The domain event is transformed into the integration event `LIBRARY_CARD_ASSIGNED` and published through RabbitMQ.

Consumer services process the event and update their own user projections, ensuring that all read models remain consistent.

This approach was chosen because the new information had to be propagated not only to the User Service projections, but also to projections maintained by other 
services, such as the Loan Service.

Unlike normal business events, `LIBRARY_CARD_ASSIGNED` is a technical migration event. 
It is generated exclusively by the migration process and is never produced by normal application workflows.

Because the event is persisted in the event store, replay operations can reconstruct projections correctly without losing card number information for legacy users.

A key advantage of this approach is that existing projections are updated automatically through the normal event-processing pipeline, without requiring a 
replay operation immediately after the migration.

### Strategy 2: Backward-Compatible Projection Logic

A different strategy was adopted for the introduction of the `returnedAt` field in the `LOAN_RETURNED` event.

The original schema did not contain the field:

```text
LOAN_RETURNED v1
```

A later version introduced an explicit return date:

```text
LOAN_RETURNED v1
        │
        ▼
LOAN_RETURNED v2
      + returnedAt
```

In this scenario, a dedicated migration event was not required.

The new information only affected projections managed inside the Loan Service and did not need to be propagated to external bounded contexts.

Although the event is also consumed by the Book Service, the additional field is not required there because the Book Service only uses the event to trigger the 
generation of a `BookReturnedEvent`.

For this reason, the missing information could be derived from data already present in historical events.

LOAN_RETURNED event handler therefore supports both schema versions:

- for v2, the handler validates that the `returnedAt` field is present;
- for v1, the loan projection derives the value from the event `occurredAt` timestamp.

This approach preserves compatibility while avoiding the complexity of an additional migration process and technical event.

Unlike the library card scenario, existing projections must be rebuilt through replay after deploying the new projection logic. 

During replay, historical v1 events are processed again and the missing `returnedAt` value is reconstructed from the original event metadata.

As a result, historical events remain fully replayable and projections can reconstruct the complete loan history even when processing older event versions.

---

## Event Sourcing

The Book Service, Loan Service and User Service implement Event Sourcing.

Instead of storing only the current state of an aggregate, the platform persists the complete sequence of events that describe how the system evolved over time.

```text
Command
    │
    ▼
Aggregate
    │
    ▼
Domain Event
    │
    ▼
Event Store
    │
    ▼
Aggregate Reconstruction
```

The current state of an aggregate is reconstructed by replaying the events that belong to its event stream.

Unlike a traditional Event Sourcing implementation that stores only locally generated domain events, each service persists both producer and consumer events in 
the same event store.

Events are distinguished through an event category:

- producer events, representing domain events generated within the service;
- consumer events, representing integration events received from external services.

By storing both event categories in the same event stream repository, every service can rebuild its projections independently without requesting data from other 
services.

The event store acts as the authoritative source of truth for each bounded context, while projections are treated as derived read models that can be rebuilt 
whenever necessary.

This approach provides several benefits:

- auditability;
- historical traceability;
- deterministic state reconstruction;
- replay support;
- projection recovery;
- temporal debugging;
- service autonomy during projection rebuilding.

Because every business change is represented as an immutable event, the platform can reconstruct historical states, investigate past business behavior and 
rebuild projections without relying on snapshots of the current state.

Event Sourcing also enables some of the most important capabilities of the platform, including replay processing, projection rebuilding and long-term schema 
evolution.

### Event Volume Considerations

The current implementation stores the complete event history without introducing snapshotting or archival strategies.

Given the expected data volume and the educational goals of the project, full event replay is considered acceptable.

In larger production environments, additional techniques such as aggregate snapshotting, event archival or event stream sharding could be introduced to manage 
long-term event growth.

---

## CQRS

The platform adopts Command Query Responsibility Segregation (CQRS) to separate write operations from read operations.

Commands modify aggregates and generate domain events, while queries operate on dedicated read models optimized for data retrieval.

```text
Commands
    │
    ▼
Aggregates
    │
    ▼
Domain Events
    │
    ▼
Event Store
    │
    ▼
Integration Events
    │
    ▼
Projectors
    │
    ▼
Read Models
    │
    ▼
Queries
```

The write side is responsible for enforcing business rules, validating commands and producing domain events.

The read side is composed of projections built from integration events and optimized for querying and reporting.

This separation provides several benefits:

- independent evolution of read and write models;
- simplified query logic;
- improved read performance;
- support for denormalized views;
- projection rebuilding through replay.

Read models are treated as derived data and can be safely deleted and rebuilt at any time from the underlying event history, without affecting the write 
side of the system.

Because projections are updated asynchronously through event processing, the platform follows an eventual consistency model between the write side and the read side.

CQRS works together with Event Sourcing and Replay infrastructure, enabling the platform to rebuild projections whenever business requirements or projection 
schemas change.

---

## Replay Architecture

The platform provides replay capabilities for rebuilding projections.

Replay supports:

- projection recovery;
- projection evolution;
- consistency validation;
- migration support.

Replay requests are distributed through dedicated RabbitMQ infrastructure.

```text
Replay Request
        │
        ▼
Replay Exchange
        │
        ├── replay.book
        ├── replay.loan
        └── replay.user
```

A replay operation begins by publishing a dedicated replay message to RabbitMQ.

The target service receives the replay request and starts rebuilding its projections using the events stored in its local event store.

To guarantee deterministic projection rebuilding, historical events are processed in the same order in which they originally occurred.

Events are retrieved from the event store ordered by:

- occurrence timestamp (`occurredAt`);
- event identifier, when multiple events share the same timestamp.

This ensures that replayed projections produce the same result as the original event-processing flow.

To avoid impacting the active read models, replay operations do not update production projections directly.

Instead, replay handlers populate temporary projection tables.

```text
   Replay Request
        │
        ▼
   Event Store
        │
        ▼
 Replay Handlers
        │
        ▼
Temporary Projections
        │
        ▼
Projection Promotion
        │
        ▼
Production Projections
```

Once all events have been processed successfully, the temporary projections replace the existing production projections through an atomic promotion process.

This approach guarantees that users never interact with partially rebuilt views and allows replay operations to be executed safely while the platform remains available.

Because each service persists both locally generated events and consumed integration events, replay operations can be executed entirely within the service 
boundary without requesting historical data from external services.

Replay capabilities are particularly important for:

- introducing new projection schemas;
- rebuilding corrupted projections;
- validating projection consistency;
- supporting event schema evolution;
- recovering historical data after migrations.

Replay works together with Event Sourcing and CQRS, ensuring that projections remain disposable artifacts that can always be reconstructed from persisted events.

---

## Reliable Event Publication (Outbox)

The platform uses the Outbox Pattern to guarantee reliable publication of integration events.

In a distributed system, persisting domain changes and publishing integration events are two separate operations.

Without additional safeguards, failures occurring between these operations can lead to inconsistencies.

For example:

```text
Domain Event Persisted
          │
          ▼
Application Crash
          │
          ▼
Event Not Published
```

In this scenario, the local state is updated successfully but other services never receive the corresponding integration event.

To avoid this problem, domain changes and integration events are persisted within the same database transaction.

```text
Aggregate
    │
    ▼
Domain Event
    │
    ├── Event Store
    │
    └── Outbox
            │
            ▼
    Outbox Publisher
            │
            ▼
         RabbitMQ
```

When a business operation succeeds:

- the domain event is stored in the event store;
- a reference to the persisted event is stored in the outbox table;
- both operations are committed atomically.

This guarantees that an event can never be persisted without its corresponding outbox entry.

A dedicated publisher process periodically scans the outbox table and publishes pending events to RabbitMQ.

Once publication succeeds, the outbox entry is marked as processed.

This approach guarantees that no integration event is lost due to temporary application failures or messaging infrastructure outages.

### Retry and Failure Handling

The Outbox Publisher continuously processes pending outbox entries in batches.

```text
Outbox Entry
      │
      ▼
Publish Attempt
      │
      ├── Success ──► Published
      │
      └── Failure
               │
               ▼
            Retry
```

Failed publication attempts are automatically retried using an exponential backoff strategy.

Events remain in the outbox until they are successfully delivered or reach the maximum retry limit.

After the retry limit is exceeded, the event is marked as failed and can be inspected through operational recovery procedures.

### Relationship with Event Sourcing

The Outbox Pattern complements Event Sourcing.

Event Sourcing guarantees that domain changes are durably persisted in the event store.

The Outbox Pattern guarantees that the corresponding integration events are eventually delivered to interested services.

Together, these patterns ensure consistency between local state changes and inter-service communication while preserving service autonomy and reliability.

---

## Messaging Topology

RabbitMQ acts as the messaging backbone of the platform.

The messaging infrastructure includes:

- service queues for integration events;
- replay queues used during projection rebuilding;
- notification queues used by the Notification Service;
- dead-letter queues used for failure recovery.

Dedicated queues isolate service workloads and support reliable event-driven communication across the platform.

---

## Messaging Reliability

The platform adopts several mechanisms to ensure reliable event consumption, projection consistency and failure recovery.

RabbitMQ provides asynchronous event delivery, while the platform implements additional reliability mechanisms such as idempotent consumers, dead-letter queues 
and local event persistence.

### Idempotent Event Processing

Integration events may be delivered more than once.

To prevent duplicate processing, each service tracks the events it has already handled and ignores events that have been processed previously.

```text
Integration Event
        │
        ▼
Already Processed?
        │
    ┌───┴───┐
    │       │
   Yes      No
    │       │
    ▼       ▼
 Ignore   Process
```

This approach guarantees idempotent event handling and prevents duplicate updates to projections.

### Event Persistence Before Processing

Consumed integration events are persisted before projection updates are executed.

```text
RabbitMQ
    │
    ▼
Persist Event
    │
    ▼
Update Projection
```

This ensures that event processing is always based on persisted data rather than transient messages.

If projection processing fails, the event remains available in the local event store and can participate in recovery and replay workflows.

### Event Ordering

The current implementation relies on RabbitMQ queue ordering.

Each service consumes messages through a single consumer configured to process one message at a time.

```text
Queue
  │
  ▼
Consumer
  │
  ▼
ACK / NACK
  │
  ▼
Next Message
```

Under the following assumptions:

- a single service instance;
- a single consumer per queue;
- sequential message processing;

events are processed in the same order in which they are delivered by RabbitMQ.

This ordering model is sufficient for the expected workload of the platform and preserves event consistency without introducing additional partitioning complexity.

The current solution favors simplicity over scalability: ordering guarantees rely on a single-consumer model. 

In larger deployments, preserving per-aggregate ordering would require a partitioning strategy based on aggregate identifiers.

In such a scenario, all events belonging to the same aggregate would need to be routed to the same logical partition and processed sequentially, while events 
belonging to different aggregates could still be processed in parallel.

This approach preserves per-aggregate ordering guarantees while allowing the platform to scale horizontally.

---

## Real-Time Notifications

The platform provides real-time user notifications through a dedicated Notification Service.

Business services publish notification-related events through RabbitMQ, decoupling notification delivery from core business workflows.

```text
Business Service
        │
        ▼
Integration Event
        │
        ▼
	RabbitMQ
        │
        ▼
Notification Service
        │
        ├── WebSocket Notifications
        └── SMS Notifications
```

This approach allows business services to remain focused on domain responsibilities while delegating user communication concerns to a dedicated component.

### WebSocket Notifications

The Notification Service exposes STOMP/WebSocket endpoints that allow clients to receive real-time updates.

Notifications are delivered through:

- topic-based broadcasts;
- user-specific destinations;
- role-protected channels.

Authentication and authorization follow the same JWT-based security model used by REST APIs.

### SMS Notifications

The platform supports SMS notifications through an abstraction layer that decouples notification workflows from the underlying SMS provider.

```text
Notification Service
        │
        ▼
    SmsSender
        │
        ▼
 SMS Provider
```

This design isolates infrastructure concerns from business logic and allows notification channels to evolve independently without affecting the notification workflow.

The current implementation uses a mock SMS provider intended for demonstration and testing purposes.
Instead of sending real SMS messages, notification requests are written to the application logs.

Because the Notification Service depends only on the `SmsSender` abstraction, a real SMS gateway can be integrated in the future without changes to the 
notification processing logic.

This approach demonstrates how external communication channels can be integrated while preserving loose coupling, testability and infrastructure independence.

---

## Observability

The platform implements distributed tracing to provide end-to-end visibility across service interactions.

In a microservices architecture, a single business operation may involve multiple services, asynchronous event exchanges and projection updates.

Distributed tracing makes it possible to follow the complete execution flow across service boundaries.

```text
Client
   │
   ▼
API Gateway
   │
   ▼
Book Service
   │
   ▼
RabbitMQ
   │
   ▼
Loan Service
   │
   ▼
Notification Service
```

Tracing information is propagated automatically across:

- HTTP requests;
- RabbitMQ messages.

### Trace Correlation

Services can access the current trace context and include trace information in application logs.

```text
Received event BOOK_RETURNED
traceId=...
spanId=...
```

The traceId identifies the entire business workflow and remains the same across all services participating in that workflow.

The spanId identifies a specific operation within the trace and changes as execution moves between services, HTTP requests and asynchronous event-processing steps.

This makes it possible to correlate log entries generated by different services and reconstruct the complete execution path of a business operation.

By combining application logs with distributed traces, operators can quickly identify where a request originated, which services participated in its execution 
and where failures or performance bottlenecks occurred.

Unlike a monolithic application, log entries are distributed across multiple independent services. 

Trace correlation allows these distributed log streams to be connected and analyzed as a single business workflow.

### Zipkin Integration

Trace information is exported to Zipkin, providing a centralized view of distributed workflows.

Zipkin allows operators to inspect:

- service dependencies;
- request execution paths;
- event-processing flows;
- latency distribution across services;
- trace and span relationships.

The Zipkin user interface can be used to visualize the complete lifecycle of a business operation as it moves across services and messaging infrastructure.

This visibility significantly simplifies troubleshooting and reduces the time required to identify failures in distributed workflows.

---

## Testing Strategy

The platform adopts a multi-layered testing strategy designed to validate business logic, service integration and end-to-end workflows.

Different testing levels focus on different architectural concerns, providing confidence that services behave correctly both in isolation and when operating as 
part of the distributed system.

### Unit and Integration Tests

The business services include automated tests implemented using JUnit.

Unit tests verify domain logic, aggregates, projections, event handlers and application services in isolation.

When infrastructure dependencies are required, tests use Testcontainers to execute against real external components rather than mocks.

This approach allows infrastructure integration scenarios to be validated while maintaining test isolation and reproducibility.

### Contract Validation

Integration event contracts are defined through versioned JSON Schemas contained in the dedicated `event-contracts` module.

These schemas represent the public contracts exchanged between services and act as the source of truth for event payload validation.

Contract validation ensures that producers and consumers continue to exchange compatible event versions as the platform evolves.

```text
Producer
    │
    ▼
JSON Schema
    │
    ▼
Consumer
```

This approach reduces the risk of breaking inter-service integrations when event schemas change.

### End-to-End Testing

The platform includes a dedicated `library-e2e-tests` module containing end-to-end tests executed against the complete running application.

Tests are written using the Gherkin language and follow a Behavior-Driven Development (BDD) approach.

```gherkin
Given ...
When ...
Then ...
```

These tests validate business workflows from the user's perspective rather than individual implementation details.

Examples include:

- user registration;
- book management;
- loan lifecycle management;
- notification workflows;
- cross-service event propagation.

Because tests execute against the real platform, they verify the interaction between services, RabbitMQ messaging, projections, security infrastructure and 
persistence layers.

### Relationship with the Architecture

The testing strategy mirrors the architecture itself:

- unit tests validate individual components and business rules;
- contract validation protects event-driven integrations;
- end-to-end tests verify complete distributed workflows.

Together, these testing layers provide confidence that architectural patterns such as Event Sourcing, CQRS, Replay, Outbox and Event-Driven communication operate 
correctly both in isolation and across service boundaries.

---

## Conclusion

This project was developed as part of a mentoring journey aimed at exploring architectural patterns and technologies beyond the traditional CRUD-based 
applications I had previously worked on.

Several of the architectural choices adopted in the platform, including Event Sourcing, CQRS, Replay and Behavior-Driven Development, were intentionally 
selected to challenge my existing experience and expose me to new ways of designing, implementing and validating distributed systems.

Throughout the project, I gained practical experience with:

- event-driven architectures;
- asynchronous communication patterns;
- Event Sourcing and CQRS;
- projection rebuilding through replay;
- contract versioning and schema evolution;
- distributed tracing and observability;
- behavior-driven testing with Gherkin.

While these architectural patterns introduce additional complexity compared to more traditional approaches, they also provide capabilities that would otherwise 
be difficult to achieve, such as complete auditability, replay-driven recovery, projection evolution and loose coupling between services.

One of the most valuable lessons learned during the project was understanding that every architectural decision involves trade-offs.

Features such as Event Sourcing, CQRS and asynchronous messaging increase flexibility and long-term evolvability, but they also require additional infrastructure,
operational concerns and development discipline.

The project therefore served not only as a software implementation exercise, but also as an opportunity to explore the reasoning, trade-offs and challenges 
involved in building modern distributed systems.