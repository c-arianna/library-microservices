package mentoring.acomi.userservice.application.aggregates;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.infrastructure.messaging.UserIntegrationPublisherEventVersions;

@Component
public class UserAggregateFactory {

	private final UserEventRepository userEventRepository;
	private final OutboxRepository outboxRepository;

	public UserAggregateFactory(UserEventRepository userEventRepository, OutboxRepository outboxRepository) {
		this.userEventRepository = userEventRepository;
		this.outboxRepository = outboxRepository;
	}

	public UserAggregate create(String userId) {
		List<UserEvent> events = userEventRepository.loadStream(userId);
		Consumer<UserEvent> dispatcher = this::persistEvent;
		return new UserAggregate(userId, dispatcher, events);
	}

	private void persistEvent(UserEvent event) {
		userEventRepository.appendToStream(event, getSchemaVersion(event.type()));
		OutboxEvent pendingOutbox = new OutboxEvent(event.eventId(), event.aggregateType(), OutboxStatus.PENDING, 0, null, 
				Instant.now(), null, Instant.now());
		outboxRepository.add(pendingOutbox);
	}

	private int getSchemaVersion(UserEventType eventType) {

		return switch (eventType) {
		case UserSubscribed -> {
			yield UserIntegrationPublisherEventVersions.USER_SUBSCRIBED;
		}
		case UserUnsubscribed -> {
			yield UserIntegrationPublisherEventVersions.USER_UNSUBSCRIBED;
		}
		case UserSuspended -> {
			yield UserIntegrationPublisherEventVersions.USER_SUSPENDED;
		}
		case UserUnsuspended -> {
			yield UserIntegrationPublisherEventVersions.USER_UNSUSPENDED;
		}
		case LibraryCardAssigned -> {
			yield UserIntegrationPublisherEventVersions.LIBRARY_CARD_ASSIGNED;
		}
		};
	}
}
