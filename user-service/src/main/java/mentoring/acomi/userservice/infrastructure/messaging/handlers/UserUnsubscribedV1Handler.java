package mentoring.acomi.userservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_UNSUBSCRIBED, supportedVersions = {1})
@Component
public class UserUnsubscribedV1Handler extends AbstractEventHandler<UserIntegrationPayload> {

	private final UserProjection projection;
	
	public UserUnsubscribedV1Handler(UserProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}

	@Override
	protected Class<UserIntegrationPayload> payloadType() {
		return UserIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(UserIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.unsubscribeUser(payload, event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.userId(), event.schemaVersion()));
	}
	
}