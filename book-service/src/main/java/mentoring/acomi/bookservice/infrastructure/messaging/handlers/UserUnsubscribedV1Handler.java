package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.projection.UserProjectionOperations;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_UNSUBSCRIBED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class UserUnsubscribedV1Handler extends AbstractEventHandler<UserIntegrationPayload> {

	private final UserProjectionOperations projectionOperations;
	
	public UserUnsubscribedV1Handler(@Qualifier("liveUserProjection") UserProjectionOperations projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}

	@Override
	protected Class<UserIntegrationPayload> payloadType() {
		return UserIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(UserIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.unsubscribeUser(payload.userId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.userId()));
	}
	
}