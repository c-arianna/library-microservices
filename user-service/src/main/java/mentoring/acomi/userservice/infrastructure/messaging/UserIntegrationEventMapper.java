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
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
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
		return envelope(e, IntegrationEventTypes.USER_UNSUSPENDED, UserIntegrationPublisherEventVersions.USER_UNSUSPENDED, payload);
	}

	private IntegrationEventEnvelope<?> getUserSuspendedIntegrationEvent(UserSuspendEvent e) {
		UserIntegrationPayload payload = new UserIntegrationPayload(e.payload().userId(), UserStatus.SUSPENDED);
		return envelope(e, IntegrationEventTypes.USER_SUSPENDED, UserIntegrationPublisherEventVersions.USER_SUSPENDED, payload);
	}

	private IntegrationEventEnvelope<?> getUserUnsubscribedIntegrationEvent(UserUnsubscribeEvent e) {
		UserIntegrationPayload payload = new UserIntegrationPayload(e.payload().userId(), UserStatus.DISABLE);
		return envelope(e, IntegrationEventTypes.USER_UNSUBSCRIBED, UserIntegrationPublisherEventVersions.USER_UNSUBSCRIBED, payload);
	}

	private IntegrationEventEnvelope<?> getUserSubscribedIntegrationEvent(UserSubscribedEvent e) {
		UserSubscribedPayload eventPayload = e.payload();
		UserSubscribedIntegrationPayload payload = new UserSubscribedIntegrationPayload(eventPayload.id(), eventPayload.email(), eventPayload.name(),
				eventPayload.lastname(), eventPayload.userIdentityProviderId(), eventPayload.status(), eventPayload.role());
		return envelope(e, IntegrationEventTypes.USER_SUBSCRIBED, UserIntegrationPublisherEventVersions.USER_SUBSCRIBED, payload);
	}
	
	private <T> IntegrationEventEnvelope<T> envelope(UserEvent e, IntegrationEventTypes type, int version, T payload) {
		return new IntegrationEventEnvelope<>(e.eventId(), type, PRODUCER, e.aggregateId(), e.occurredAt(), version, payload);
	}

}
