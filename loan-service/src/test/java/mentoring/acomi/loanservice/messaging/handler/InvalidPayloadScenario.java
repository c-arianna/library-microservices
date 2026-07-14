package mentoring.acomi.loanservice.messaging.handler;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public record InvalidPayloadScenario(String description, String field, IntegrationEventEnvelope<?> event) {}
