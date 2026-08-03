package mentoring.acomi.loanservice.application.projection;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

public interface EventProjector {
    boolean supports(IntegrationEventTypes eventType);
    void project(IntegrationEventEnvelope<?> event);
}