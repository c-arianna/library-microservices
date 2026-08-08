package mentoring.acomi.notificationservice.messaging.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

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

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookRequestStatus;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookRequestUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.BookRequestUpdatedV1NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BookRequestUpdatedNotificationHandlerTest {

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	private ObjectMapper mapper = new ObjectMapper();

	private BookRequestUpdatedV1NotificationHandler handler;

	@BeforeEach
	void setUp() {
		handler = new BookRequestUpdatedV1NotificationHandler(mapper, messagingTemplate);
	}

	@Test
	void shouldSendNotification() {

		NotificationEventEnvelope<BookRequestUpdatedNotificationPayload> event = buildBookRequestUpdatedEvent();

		handler.handleEvent(event);

		ArgumentCaptor<EventNotification> eventCaptor = ArgumentCaptor.forClass(EventNotification.class);

		verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/bookRequests"), eventCaptor.capture());
		verifyNoMoreInteractions(messagingTemplate);

		EventNotification sentEvent = eventCaptor.getValue();

		Assertions.assertEquals(NotificationEventType.BOOK_REQUEST_UPDATED, sentEvent.eventType());

		BookRequestUpdatedNotificationPayload payload = (BookRequestUpdatedNotificationPayload) sentEvent.payload();

		BookRequestUpdatedNotificationPayload expectedPayload = event.payload();

		Assertions.assertAll(() -> Assertions.assertEquals(expectedPayload.isbn(), payload.isbn()),
				() -> Assertions.assertEquals(expectedPayload.author(), payload.author()),
				() -> Assertions.assertEquals(expectedPayload.title(), payload.title()),
				() -> Assertions.assertEquals(expectedPayload.requestId(), payload.requestId()),
				() -> Assertions.assertEquals(expectedPayload.votes(), payload.votes()),
				() -> Assertions.assertEquals(expectedPayload.status(), payload.status()));

	}

	@Test
	void shouldRejectNullPayload() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.BOOK_REQUEST_UPDATED, "book-service", Instant.now(), 1, null);

		NotificationHandlingException ex  = Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));
		Assertions.assertEquals("Payload is null", ex.getMessage());
		
		verifyNoInteractions(messagingTemplate);
	}

	private NotificationEventEnvelope<BookRequestUpdatedNotificationPayload> buildBookRequestUpdatedEvent() {
		return new NotificationEventEnvelope<BookRequestUpdatedNotificationPayload>(UUID.randomUUID().toString(), 
				NotificationEventType.BOOK_REQUEST_UPDATED, "book-service", Instant.now(), 1, getPayload());
	}

	private BookRequestUpdatedNotificationPayload getPayload() {
		return new BookRequestUpdatedNotificationPayload("REQ-1", "Italo Calvino", "Il barone rampante", "", 2, BookRequestStatus.PENDING);
	}

}
