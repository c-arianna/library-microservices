package mentoring.acomi.loanservice.application.projection;

import java.util.List;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

@Component
public class DefaultProjectionDispatcher implements ProjectionDispatcher {

    private final List<EventProjector> projectors;

    public DefaultProjectionDispatcher(List<EventProjector> projectors) {
        this.projectors = projectors;
    }

    @Override
    public void dispatch(IntegrationEventEnvelope<?> event, Object payload) {
    	 IntegrationEventEnvelope<?> typedEvent = new IntegrationEventEnvelope<>(event.eventId(), event.eventType(),
                         event.producer(), event.aggregateId(), event.aggregateType(), event.eventVersion(),
                         event.occurredAt(), event.schemaVersion(), payload);
        projectors.stream().filter(p -> p.supports(event.eventType())).forEach(p -> p.project(typedEvent));
    }
}