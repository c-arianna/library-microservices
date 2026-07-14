package mentoring.acomi.userservice.messaging.handlers;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public record InvalidPayloadScenario(String description, String field, IntegrationEventEnvelope<?> event) {}