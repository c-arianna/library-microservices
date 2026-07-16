package mentoring.acomi.sharedcorelibrary.integration.messaging;

import java.util.Optional;

public interface EventHandler {
	Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> event);
}