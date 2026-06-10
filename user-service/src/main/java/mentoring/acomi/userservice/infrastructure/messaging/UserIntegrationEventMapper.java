package mentoring.acomi.userservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

@Component
public class UserIntegrationEventMapper {
	
	private static final String PRODUCER = "user-service";
	
	public IntegrationEventEnvelope<?> map(UserEvent event) {
		return switch (event) {
		case UserSubscribedEvent e -> {
			yield getUserSubscribedIntegrationEvent(e);
		}
		case UserUnsubscribeEvent e ->{
			yield getUserUnsubscribedIntegrationEvent(e);
		}
		case UserSuspendEvent e -> {
			yield getUserSuspendedIntegrationEvent(e);
		}
		case UserUnsuspendedEvent e -> {
			yield getUserUnsuspendedIntegrationEvent(e);
		}
	
		};
	}

	private IntegrationEventEnvelope<?> getUserUnsuspendedIntegrationEvent(UserUnsuspendedEvent e) {
		UserIntegrationPayload payload = new UserIntegrationPayload(e.payload().userId(), UserStatus.ACTIVE);
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.USER_UNSUSPENDED, PRODUCER, e.aggregateId(), e.occurredAt(), 
				UserIntegrationEventVersions.USER_UNSUSPENDED, payload);
	}

	private IntegrationEventEnvelope<?> getUserSuspendedIntegrationEvent(UserSuspendEvent e) {
		UserIntegrationPayload payload = new UserIntegrationPayload(e.payload().userId(), UserStatus.SUSPENDED);
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.USER_SUSPENDED, PRODUCER, e.aggregateId(), e.occurredAt(), 
				UserIntegrationEventVersions.USER_SUSPENDED, payload);
	}

	private IntegrationEventEnvelope<?> getUserUnsubscribedIntegrationEvent(UserUnsubscribeEvent e) {
		UserIntegrationPayload payload = new UserIntegrationPayload(e.payload().userId(), UserStatus.DISABLE);
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.USER_UNSUBSCRIBED, PRODUCER, e.aggregateId(), e.occurredAt(), 
				UserIntegrationEventVersions.USER_UNSUBSCRIBED, payload);
	}

	private IntegrationEventEnvelope<?> getUserSubscribedIntegrationEvent(UserSubscribedEvent e) {
		UserSubscribedIntegrationPayload payload = new UserSubscribedIntegrationPayload(e.payload().id(), e.payload().email(), e.payload().status());
		return new IntegrationEventEnvelope<>(e.eventId(), IntegrationEventTypes.USER_SUBSCRIBED, PRODUCER, e.aggregateId(), e.occurredAt(), 
				UserIntegrationEventVersions.USER_SUBSCRIBED, payload);
	}

}
