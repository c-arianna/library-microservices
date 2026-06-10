package mentoring.acomi.userservice.event.mapping;

import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedlibrary.model.UserRole;
import mentoring.acomi.sharedlibrary.model.UserStatus;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.domain.events.payload.UserPayload;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserUnsubscribedPayload;
import mentoring.acomi.userservice.infrastructure.messaging.UserIntegrationEventMapper;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

public class UserIntegrationEventMapperTest {

	private static final String USER_UNSUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_UNSUSPENDED.eventName;
	private static final String USER_SUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_SUSPENDED.eventName;
	private static final String USER_UNSUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_UNSUBSCRIBED.eventName;
	private static final String USER_SUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_SUBSCRIBED.eventName;
	
	private static final String PRODUCER = "user-service";
	private final UserIntegrationEventMapper mapper = new UserIntegrationEventMapper();
	
	@ParameterizedTest(name = "[{index}] set correct metadata -> {0}")
	@MethodSource("eventCases")
	void shouldSetCorrectMetadata(String name, UserEvent domainEvent, IntegrationEventTypes eventType) {

		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);

		Assertions.assertEquals(eventType, event.eventType());
		Assertions.assertEquals(PRODUCER, event.producer());
		Assertions.assertEquals(1, event.schemaVersion());
		Assertions.assertNotNull(event.eventId());
	}
	

	@Test
	void shouldMapUserIntegrationPayloadCorrectly() {
		
		UserUnsubscribeEvent domainEvent = getUserUnsubscribedEvent();
		
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
		
		UserIntegrationPayload integrationPayload = (UserIntegrationPayload) event.payload();
		UserUnsubscribedPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.userId(), integrationPayload.userId());
		Assertions.assertEquals(UserStatus.DISABLE, integrationPayload.status());
		
	}
	
	@Test
	void shouldMapUserSubscribedIntegrationPayloadCorrectly() {
		
		UserSubscribedEvent domainEvent = getUserSubscribedEvent();
		
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
		
		UserSubscribedIntegrationPayload integrationPayload = (UserSubscribedIntegrationPayload) event.payload();
		UserSubscribedPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.id(), integrationPayload.userId());
		Assertions.assertEquals(payload.email(), integrationPayload.email());
		Assertions.assertEquals(payload.status(), integrationPayload.status());
		
	}
	
	
	static Stream<Arguments> eventCases() {
		return Stream.of(Arguments.of(USER_SUBSCRIBED_EVENT_NAME, getUserSubscribedEvent(), IntegrationEventTypes.USER_SUBSCRIBED),
				Arguments.of(USER_UNSUBSCRIBED_EVENT_NAME, getUserUnsubscribedEvent(), IntegrationEventTypes.USER_UNSUBSCRIBED),
				Arguments.of(USER_SUSPENDED_EVENT_NAME, getUserSuspendedEvent(), IntegrationEventTypes.USER_SUSPENDED),
				Arguments.of(USER_UNSUSPENDED_EVENT_NAME, getUserUnsuspendedEvent(), IntegrationEventTypes.USER_UNSUSPENDED));
	}
	
	private static UserUnsuspendedEvent getUserUnsuspendedEvent() {
		UserPayload payload = new UserPayload("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "test@gmail.com", "Policy Violation", "admin1");
		return new UserUnsuspendedEvent("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "d50cc664-0391-4833-8414-18c4c9e1bd45", payload, Instant.now());
	}

	private static UserSuspendEvent getUserSuspendedEvent() {
		UserPayload payload = new UserPayload("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "test@gmail.com", "Policy Violation", "admin1");
		return new UserSuspendEvent("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "92e57621-d491-4c02-9a21-d910107e71d0", payload, Instant.now());
	}

	private static UserUnsubscribeEvent getUserUnsubscribedEvent() {
		UserUnsubscribedPayload payload = new UserUnsubscribedPayload("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "test@gmail.com", "Unsubscribed");
		return new UserUnsubscribeEvent("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "9c8db1b1-38d0-4717-aa34-702365299081", payload, Instant.now()); 
	}

	private static UserSubscribedEvent getUserSubscribedEvent() {
		UserSubscribedPayload payload = new UserSubscribedPayload("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "test@gmail.com", "Test", "Test", "123456789",
				UserStatus.ACTIVE, UserRole.READER);
		return new UserSubscribedEvent("148a2b0c-1c3c-4e81-b522-5c4a07f71a9a", "07bf89ea-fd4e-4080-8fda-eb94679c4f6d", payload, Instant.now());
    }
}
