# Library E2E Tests

← [Back to the main README](../README.md)

📖 [Architecture Documentation](../docs/architecture.md)

## Overview

The Library E2E Tests module validates the platform from an end-user perspective through end-to-end scenarios executed against a fully deployed environment.

Tests exercise the platform through its public APIs and verify the complete interaction between microservices, authentication, asynchronous messaging and CQRS read 
models.

The module uses Cucumber and Gherkin to describe business behaviors in a human-readable format while executing automated validation against the running platform.

## Purpose

The test suite provides confidence that the platform behaves correctly as a whole and that business workflows continue to operate correctly across service boundaries.

The suite verifies:

- API integration;
- authentication and authorization;
- inter-service communication;
- event-driven workflows;
- eventual consistency behavior;
- projection updates;
- business process execution.

## Testing Approach

Tests are written using behavior Driven Development (BDD) principles.

Business scenarios are expressed through:

- Features;
- Rules;
- Scenarios;
- Gherkin steps.

The scenarios are written from a business perspective and are intended to validate platform behavior rather than implementation details.

## Environment Isolation

Tests are designed to execute exclusively against the dedicated Gherkin environment.

The suite validates the target environment configuration before execution to prevent accidental execution against development environments.

A dedicated Docker Compose profile provides:

- isolated databases;
- isolated RabbitMQ infrastructure;
- isolated Keycloak realm;
- isolated service instances.

## Authentication and Security

Tests authenticate against a real Keycloak instance and obtain JWT tokens through OAuth2 flows before interacting with protected APIs.

Role-based workflows are validated using different user profiles, including:

- Reader;
- Librarian;
- Administrator.

## Covered Business Domains

### Book Management

Representative scenarios include:

- book registration;
- catalogue browsing;
- book detail visualization;
- copy management;
- purchase requests;
- purchase request voting;
- purchase request approval and rejection;
- purchase suggestion generation;
- book availability subscriptions.

### Loan Management

Representative scenarios include:

- loan requests;
- reservation confirmation;
- loan cancellation;
- loan returns;
- loan detail visualization;
- overdue loan monitoring;
- popular book statistics;
- user loan statistics.

### User Management

Representative scenarios include:

- user registration;
- user unsubscription;
- user suspension;
- user reactivation;
- profile management;
- administrative user creation;
- library card generation.

## Eventual Consistency Validation

The platform relies heavily on asynchronous processing and event-driven communication.

The test suite validates eventual consistency by polling read models and projections until the expected state becomes available.

This approach allows the suite to verify:

- projection updates;
- asynchronous event processing;
- inter-service synchronization;
- distributed workflow completion.

## Test Infrastructure

The suite includes supporting components for:

- authentication management;
- shared test context;
- dynamic parameter resolution;
- reusable assertions;
- asynchronous verification;
- automatic cleanup.

Test data generated during execution is automatically removed at the end of each scenario to guarantee isolation and repeatability.

### Technology Stack

- Java 21
- JUnit 5
- Cucumber
- Gherkin
- Spring RestTestClient
- JsonPath