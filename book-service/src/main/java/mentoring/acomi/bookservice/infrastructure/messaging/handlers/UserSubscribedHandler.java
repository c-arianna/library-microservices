package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import mentoring.acomi.bookservice.application.projection.UserProjectionOperations;
import mentoring.acomi.bookservice.application.projection.UserSubscriptionData;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.model.UserRole;

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
		
		if (event.schemaVersion() >= 2) {
			
			boolean hasCardNumber = StringUtils.hasText(payload.cardNumber());
			boolean invalidReader = payload.role() == UserRole.READER && !hasCardNumber;
			boolean invalidOperator = payload.role() != UserRole.READER && hasCardNumber;
			
			if (invalidReader || invalidOperator) {
				throw new IllegalStateException("invalid card number for schema version 2");
			}
		}
		
		projectionOperations.subscribeUser(getSubscriptionData(payload), event.occurredAt());
	
		return Optional.empty();
		
	}

	private UserSubscriptionData getSubscriptionData(UserSubscribedIntegrationPayload payload) {
		return new UserSubscriptionData(payload.userId(), payload.email(), payload.name(), payload.lastname(),
				payload.userIdentityProviderId(), payload.cardNumber(), payload.status());
	}
}
