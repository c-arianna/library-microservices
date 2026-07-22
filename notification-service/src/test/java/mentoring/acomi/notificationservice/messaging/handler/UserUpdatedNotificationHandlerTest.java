package mentoring.acomi.notificationservice.messaging.handler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.UserUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.UserUpdatedV1NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserUpdatedNotificationHandlerTest {
	
	@Mock
	private SimpMessagingTemplate messagingTemplate;

	private ObjectMapper mapper = new ObjectMapper();
	
	private UserUpdatedV1NotificationHandler handler;
	
	@BeforeEach
	void setUp() {
		handler = new UserUpdatedV1NotificationHandler(mapper, messagingTemplate);
	}
	
	@Test
	void shouldSendNotification() {

		NotificationEventEnvelope<UserUpdatedNotificationPayload> event = buildUserUpdatedEvent();

		handler.handleEvent(event);

		ArgumentCaptor<EventNotification> eventCaptor = ArgumentCaptor.forClass(EventNotification.class);

		verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/users"), eventCaptor.capture());
		verify(messagingTemplate, times(1)).convertAndSendToUser(eq(event.payload().userIdentityProviderId()), eq("/queue/users"), eventCaptor.capture());
		verifyNoMoreInteractions(messagingTemplate);
		
		EventNotification sentEvent = eventCaptor.getValue();

		Assertions.assertEquals(NotificationEventType.USER_UPDATED, sentEvent.eventType());

		UserUpdatedNotificationPayload payload = (UserUpdatedNotificationPayload) sentEvent.payload();

		UserUpdatedNotificationPayload expectedPayload = event.payload();

		Assertions.assertAll(
				() -> Assertions.assertEquals(expectedPayload.userId(), payload.userId()),
				() -> Assertions.assertEquals(expectedPayload.email(), payload.email()),
				() -> Assertions.assertEquals(expectedPayload.name(), payload.name()),
				() -> Assertions.assertEquals(expectedPayload.lastname(), payload.lastname()),
				() -> Assertions.assertEquals(expectedPayload.userIdentityProviderId(), payload.userIdentityProviderId()),
				() -> Assertions.assertEquals(expectedPayload.cardNumber(), payload.cardNumber()),
				() -> Assertions.assertEquals(expectedPayload.status(), payload.status()),
				() -> Assertions.assertEquals(expectedPayload.role(), payload.role()));
	}

	@Test
	void shouldSupportVersion1() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.USER_UPDATED, "user-service", Instant.now(), 1, Map.of());

		Assertions.assertTrue(handler.accepts(event));
	}

	@Test
	void shouldNotSupportUnknownSchemaVersion() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.USER_UPDATED, "user-service", Instant.now(), 999, Map.of());

		Assertions.assertFalse(handler.accepts(event));
	}

	@Test
	void shouldNotSupportDifferentEventType() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.LOAN_UPDATED, "loan-service", Instant.now(), 1, Map.of());

		Assertions.assertFalse(handler.accepts(event));
	}

	@Test
	void shouldRejectNullPayload() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.USER_UPDATED, "user-service", Instant.now(), 1, null);

		NotificationHandlingException ex  = Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));
		Assertions.assertEquals("Payload is null", ex.getMessage());
		
		verifyNoInteractions(messagingTemplate);
	}

	private NotificationEventEnvelope<UserUpdatedNotificationPayload> buildUserUpdatedEvent() {
		return new NotificationEventEnvelope<UserUpdatedNotificationPayload>(UUID.randomUUID().toString(), 
				NotificationEventType.USER_UPDATED, "USER-service", Instant.now(), 1, getPayload());
	}

	private UserUpdatedNotificationPayload getPayload() {
		return new UserUpdatedNotificationPayload(UUID.randomUUID().toString(), "test@gmail.com", "Test", "Test", UUID.randomUUID().toString(),
				"LIB-000001", UserStatus.ACTIVE, UserRole.READER);
	}

}
