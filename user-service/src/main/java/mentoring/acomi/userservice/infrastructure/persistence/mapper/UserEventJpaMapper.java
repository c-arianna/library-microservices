package mentoring.acomi.userservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedlibrary.eventstore.EventMapper;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserPayload;
import mentoring.acomi.userservice.domain.events.payload.UserUnsubscribedPayload;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class UserEventJpaMapper implements EventMapper<UserEvent, UserEventEntity> {

	private final ObjectMapper objectMapper;

	public UserEventJpaMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public UserEvent toDomain(UserEventEntity event) {

		UserEventType eventType = UserEventType.valueOf(event.getEventType());

		return getEvent(eventType, event);
	}

	private UserEvent getEvent(UserEventType eventType, UserEventEntity event) {

		return switch (eventType) {

		case UserSubscribed -> {
			UserSubscribedPayload payload = objectMapper.treeToValue(event.getPayload(), UserSubscribedPayload.class);
			yield new UserSubscribedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
		}

		case UserUnsubscribed -> {
			UserUnsubscribedPayload payload = objectMapper.treeToValue(event.getPayload(),
					UserUnsubscribedPayload.class);
			yield new UserUnsubscribeEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
		}

		case UserSuspended -> {
			UserPayload payload = objectMapper.treeToValue(event.getPayload(), UserPayload.class);
			yield new UserSuspendEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
		}

		case UserUnsuspended -> {
			UserPayload payload = objectMapper.treeToValue(event.getPayload(), UserPayload.class);
			yield new UserUnsuspendedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
		}

		};

	}

	public UserEventEntity toEntity(UserEvent event) {
		return new UserEventEntity(event.aggregateId(), event.type().name(), event.eventId(),
				toJsonNode(event.payload()), event.occurredAt());

	}

	private JsonNode toJsonNode(Object payload) {
		return objectMapper.valueToTree(payload);
	}

}
