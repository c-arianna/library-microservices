package mentoring.acomi.notificationservice.messaging.handler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.UserUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.UserUpdatedNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@ExtendWith(MockitoExtension.class)
public class UserUpdatedNotificationHandlerTest extends AbstractNotificationHandlerTest {
	
	@Mock
	private SimpMessagingTemplate messagingTemplate;
	
	private UserUpdatedNotificationHandler handler;
	
	@BeforeEach
	void setUp() {
		handler = new UserUpdatedNotificationHandler(mapper, messagingTemplate);
	}
	
	@Override
	protected NotificationHandler handler() {
		return handler;
	}

	@Override
	protected NotificationEventEnvelope<?> validEvent() {
		return buildUserUpdatedEvent();
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

	private NotificationEventEnvelope<UserUpdatedNotificationPayload> buildUserUpdatedEvent() {
		return new NotificationEventEnvelope<UserUpdatedNotificationPayload>(UUID.randomUUID().toString(), 
				NotificationEventType.USER_UPDATED, "USER-service", Instant.now(), 1, getPayload());
	}

	private UserUpdatedNotificationPayload getPayload() {
		return new UserUpdatedNotificationPayload(UUID.randomUUID().toString(), "test@gmail.com", "Test", "Test", UUID.randomUUID().toString(),
				"LIB-000001", UserStatus.ACTIVE, UserRole.READER);
	}

}
