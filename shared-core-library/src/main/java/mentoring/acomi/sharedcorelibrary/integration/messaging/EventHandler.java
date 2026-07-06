package mentoring.acomi.sharedcorelibrary.integration.messaging;

public interface EventHandler {
    IntegrationEventTypes eventType();
    boolean accepts(IntegrationEventEnvelope<?> event);
    void handleEvent(IntegrationEventEnvelope<?> event);
}