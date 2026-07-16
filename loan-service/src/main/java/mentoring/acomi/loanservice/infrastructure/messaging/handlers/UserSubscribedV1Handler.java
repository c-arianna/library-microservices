package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_SUBSCRIBED, supportedVersions = {1})
@Component
public class UserSubscribedV1Handler extends AbstractEventHandler<UserSubscribedIntegrationPayload> {

	private final UserProjection userProjection;

	public UserSubscribedV1Handler(UserProjection userProjection, EventPayloadMapper mapper) {
		super(mapper);
		this.userProjection = userProjection;
	}
	
	@Override
	protected Class<UserSubscribedIntegrationPayload> payloadType() {
		return UserSubscribedIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(UserSubscribedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		userProjection.handleSubscribeUser(payload, event.occurredAt());
		return Optional.empty();
	}

}
