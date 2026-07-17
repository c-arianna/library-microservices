package mentoring.acomi.sharedcorelibrary.integration.messaging;

import java.util.Optional;

public abstract class NoOpEventHandler implements EventHandler {

	@Override
	public Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> event) {
		return Optional.empty();
	}
}
