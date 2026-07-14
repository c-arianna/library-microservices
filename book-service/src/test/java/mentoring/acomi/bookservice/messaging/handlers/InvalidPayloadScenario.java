package mentoring.acomi.bookservice.messaging.handlers;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public record InvalidPayloadScenario(String description, String field, IntegrationEventEnvelope<?> event) {}