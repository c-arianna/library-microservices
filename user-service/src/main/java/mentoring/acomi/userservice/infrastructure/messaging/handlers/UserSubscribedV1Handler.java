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
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_SUBSCRIBED, supportedVersions = {1})
@Component
public class UserSubscribedV1Handler extends AbstractEventHandler<UserSubscribedIntegrationPayload> {

	private final UserProjection projection;
		
	public UserSubscribedV1Handler(UserProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}
	
    @Override
	protected Class<UserSubscribedIntegrationPayload> payloadType() {
		return UserSubscribedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(UserSubscribedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.subscribeUser(payload, event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.userId(), event.schemaVersion()));
	}
	
}
