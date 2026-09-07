# Infrastructure

← [Back to the main README](../README.md)

📖 [Architecture Documentation](../docs/architecture.md)

## Overview

This module contains the infrastructure required to run the Library Platform locally.

The environment is fully containerized and provisioned through Docker Compose.

## Provided Components

The infrastructure includes:

- API Gateway
- Book Service
- Loan Service
- User Service
- Notification Service
- RabbitMQ
- Keycloak
- MySQL databases
- Zipkin

The infrastructure provisions both application services and supporting platform components required by the distributed architecture.

## Environment Profiles

The environments share the same architectural topology while allowing different operational configurations for development and testing purposes.

Two environments are provided:

### Development Environment

Used for local development and manual testing.

Persistent storage is enabled for databases and RabbitMQ.

### Gherkin Environment

Used by the end-to-end test suite.

Services run using isolated infrastructure and temporary storage to guarantee repeatable test execution.

## Messaging Infrastructure

RabbitMQ is automatically provisioned with:

- exchanges;
- queues;
- dead-letter queues;
- replay queues;
- notification queues.

These components support asynchronous communication, reliable event processing, event replay and notification delivery across the platform.

## Identity Management

Keycloak is automatically configured with:

- realms;
- roles;
- clients;
- test users;
- service accounts;
- custom login theme.

## Database Initialization

The infrastructure initializes all service databases and provides the schemas required by Event Sourcing and CQRS read models.

## Observability

Distributed tracing is enabled through Zipkin and is available in both development and testing environments.