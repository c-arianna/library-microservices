package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.UserProjectionOperations;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.USER_SUBSCRIBED, supportedVersions = {1,2}, mode = HandlerMode.REPLAYABLE)
@Component
public class UserSubscribedHandler extends AbstractEventHandler<UserSubscribedIntegrationPayload> {

	private final UserProjectionOperations projectionOperations;

	public UserSubscribedHandler(@Qualifier("liveUserProjection") UserProjectionOperations projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}
	
	@Override
	protected Class<UserSubscribedIntegrationPayload> payloadType() {
		return UserSubscribedIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(UserSubscribedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		
		if (event.schemaVersion() >= 2 && !StringUtils.hasText(payload.cardNumber())) {
		    throw new IllegalStateException("cardNumber is mandatory for schema version 2");
		}
		
		projectionOperations.handleSubscribeUser(payload, event.occurredAt());
		return Optional.empty();
	}

}
