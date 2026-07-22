package mentoring.acomi.userservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.LibraryCardAssignedEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.domain.events.payload.LibraryCardAssignedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.LibraryCardAssignedIntegrationPayload;
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
		case LibraryCardAssignedEvent e -> {
			yield getLibraryCardAssignedIntegrationEvent(e);
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
		UserIntegrationPayload payload = new UserIntegrationPayload(e.payload().userId(), UserStatus.DISABLED);
		return envelope(e, IntegrationEventTypes.USER_UNSUBSCRIBED, UserIntegrationPublisherEventVersions.USER_UNSUBSCRIBED, payload);
	}

	private IntegrationEventEnvelope<?> getUserSubscribedIntegrationEvent(UserSubscribedEvent e) {
		UserSubscribedPayload eventPayload = e.payload();
		UserSubscribedIntegrationPayload payload = new UserSubscribedIntegrationPayload(eventPayload.id(), eventPayload.email(), eventPayload.name(),
				eventPayload.lastname(), eventPayload.userIdentityProviderId(), eventPayload.cardNumber(), 
				eventPayload.status(), eventPayload.role());
		return envelope(e, IntegrationEventTypes.USER_SUBSCRIBED, UserIntegrationPublisherEventVersions.USER_SUBSCRIBED, payload);
	}
	
	private IntegrationEventEnvelope<?> getLibraryCardAssignedIntegrationEvent(LibraryCardAssignedEvent e) {
		LibraryCardAssignedPayload eventPayload = e.payload();
		LibraryCardAssignedIntegrationPayload payload = new LibraryCardAssignedIntegrationPayload(eventPayload.userId(),
				eventPayload.cardNumber());
		return envelope(e, IntegrationEventTypes.LIBRARY_CARD_ASSIGNED, 
				UserIntegrationPublisherEventVersions.LIBRARY_CARD_ASSIGNED, payload);
	}
	
	private <T> IntegrationEventEnvelope<T> envelope(UserEvent e, IntegrationEventTypes type, int version, T payload) {
		return new IntegrationEventEnvelope<>(e.eventId(), type, PRODUCER, e.aggregateId(), AggregateType.USER.name(), e.eventVersion(), e.occurredAt(), version, payload);
	}

}
