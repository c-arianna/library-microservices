package mentoring.acomi.userservice.event.mapping;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.LibraryCardAssignedEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.domain.events.payload.LibraryCardAssignedPayload;
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
	private static final String LIBRARY_CARD_ASSIGNED_EVENT_NAME = IntegrationEventTypes.LIBRARY_CARD_ASSIGNED.eventName;
	
	private static final String CARD_NUMBER = "LIB-000001";
	
	private static final int VERSION_1 = 1;
	private static final int VERSION_2 = 2;
	
	private static final String USER_ID = UUID.randomUUID().toString();
	private static final String EVENT_ID = UUID.randomUUID().toString();
	
	private static final String PRODUCER = "user-service";
	private final UserIntegrationEventMapper mapper = new UserIntegrationEventMapper();
	
	@ParameterizedTest(name = "[{index}] set correct metadata -> {0}")
	@MethodSource("eventCases")
	void shouldSetCorrectMetadata(String name, UserEvent domainEvent, IntegrationEventTypes eventType, int eventVersion) {

		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);

		Assertions.assertEquals(eventType, event.eventType());
		Assertions.assertEquals(PRODUCER, event.producer());
		Assertions.assertEquals(eventVersion, event.schemaVersion());
		Assertions.assertNotNull(event.eventId());
	}
	

	@Test
	void shouldMapUserIntegrationPayloadCorrectly() {
		
		UserUnsubscribeEvent domainEvent = getUserUnsubscribedEvent();
		
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
		
		UserIntegrationPayload integrationPayload = (UserIntegrationPayload) event.payload();
		UserUnsubscribedPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.userId(), integrationPayload.userId());
		Assertions.assertEquals(UserStatus.DISABLED, integrationPayload.status());
		
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
		return Stream.of(Arguments.of(USER_SUBSCRIBED_EVENT_NAME, getUserSubscribedEvent(), IntegrationEventTypes.USER_SUBSCRIBED, VERSION_2),
				Arguments.of(USER_UNSUBSCRIBED_EVENT_NAME, getUserUnsubscribedEvent(), IntegrationEventTypes.USER_UNSUBSCRIBED, VERSION_1),
				Arguments.of(USER_SUSPENDED_EVENT_NAME, getUserSuspendedEvent(), IntegrationEventTypes.USER_SUSPENDED, VERSION_1),
				Arguments.of(LIBRARY_CARD_ASSIGNED_EVENT_NAME, getLibraryCardAssignedEvent(), IntegrationEventTypes.LIBRARY_CARD_ASSIGNED, VERSION_1),
				Arguments.of(USER_UNSUSPENDED_EVENT_NAME, getUserUnsuspendedEvent(), IntegrationEventTypes.USER_UNSUSPENDED, VERSION_1));
	}
	
	private static UserUnsuspendedEvent getUserUnsuspendedEvent() {
		UserPayload payload = new UserPayload(USER_ID, "test@gmail.com", "Policy Violation", "admin1");
		return new UserUnsuspendedEvent(USER_ID, EVENT_ID, 0, payload, Instant.now());
	}

	private static UserSuspendEvent getUserSuspendedEvent() {
		UserPayload payload = new UserPayload(USER_ID, "test@gmail.com", "Policy Violation", "admin1");
		return new UserSuspendEvent(USER_ID, EVENT_ID, 0, payload, Instant.now());
	}

	private static UserUnsubscribeEvent getUserUnsubscribedEvent() {
		UserUnsubscribedPayload payload = new UserUnsubscribedPayload(USER_ID, "test@gmail.com", "Unsubscribed");
		return new UserUnsubscribeEvent(USER_ID, EVENT_ID, 0, payload, Instant.now()); 
	}

	private static UserSubscribedEvent getUserSubscribedEvent() {
		String identityId = UUID.randomUUID().toString();
		UserSubscribedPayload payload = new UserSubscribedPayload(USER_ID, "test@gmail.com", "Test", "Test", identityId, 
				CARD_NUMBER, UserStatus.ACTIVE, UserRole.READER);
		return new UserSubscribedEvent(USER_ID, EVENT_ID, 0, payload, Instant.now());
    }
	
	private static LibraryCardAssignedEvent getLibraryCardAssignedEvent() {
		LibraryCardAssignedPayload payload = new LibraryCardAssignedPayload(USER_ID, CARD_NUMBER);
		return new LibraryCardAssignedEvent(USER_ID, EVENT_ID, 0, payload, Instant.now());
	}

}
