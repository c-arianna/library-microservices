package mentoring.acomi.loanservice.application.projection;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public interface ProjectionDispatcher {
    void dispatch(IntegrationEventEnvelope<?> event, Object payload);
}
