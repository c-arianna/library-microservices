package mentoring.acomi.sharedcorelibrary.integration.messaging;

public interface EventHandler {
    void handleEvent(IntegrationEventEnvelope<?> event);
}