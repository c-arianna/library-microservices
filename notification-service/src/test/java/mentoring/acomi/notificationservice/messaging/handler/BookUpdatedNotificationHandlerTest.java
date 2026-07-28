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

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.BookUpdatedV1NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BookUpdatedNotificationHandlerTest {

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	private ObjectMapper mapper = new ObjectMapper();

	private BookUpdatedV1NotificationHandler handler;

	@BeforeEach
	void setUp() {
		handler = new BookUpdatedV1NotificationHandler(mapper, messagingTemplate);
	}

	@Test
	void shouldSendNotification() {

		NotificationEventEnvelope<BookUpdatedNotificationPayload> event = buildBookUpdatedEvent();

		handler.handleEvent(event);

		ArgumentCaptor<EventNotification> eventCaptor = ArgumentCaptor.forClass(EventNotification.class);

		verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/books"), eventCaptor.capture());
		verifyNoMoreInteractions(messagingTemplate);

		EventNotification sentEvent = eventCaptor.getValue();

		Assertions.assertEquals(NotificationEventType.BOOK_UPDATED, sentEvent.eventType());

		BookUpdatedNotificationPayload payload = (BookUpdatedNotificationPayload) sentEvent.payload();

		BookUpdatedNotificationPayload expectedPayload = event.payload();

		Assertions.assertAll(() -> Assertions.assertEquals(expectedPayload.isbn(), payload.isbn()),
				() -> Assertions.assertEquals(expectedPayload.author(), payload.author()),
				() -> Assertions.assertEquals(expectedPayload.title(), payload.title()),
				() -> Assertions.assertEquals(expectedPayload.description(), payload.description()),
				() -> Assertions.assertEquals(expectedPayload.available(), payload.available()),
				() -> Assertions.assertEquals(expectedPayload.totalCopies(), payload.totalCopies()),
				() -> Assertions.assertEquals(expectedPayload.reservedCopies(), payload.reservedCopies()),
				() -> Assertions.assertEquals(expectedPayload.borrowedCopies(), payload.borrowedCopies()));

	}

	@Test
	void shouldRejectNullPayload() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.BOOK_UPDATED, "book-service", Instant.now(), 1, null);

		NotificationHandlingException ex  = Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));
		Assertions.assertEquals("Payload is null", ex.getMessage());
		
		verifyNoInteractions(messagingTemplate);
	}

	private NotificationEventEnvelope<BookUpdatedNotificationPayload> buildBookUpdatedEvent() {
		return new NotificationEventEnvelope<BookUpdatedNotificationPayload>(UUID.randomUUID().toString(), 
				NotificationEventType.BOOK_UPDATED, "book-service", Instant.now(), 1, getPayload());
	}

	private BookUpdatedNotificationPayload getPayload() {
		return new BookUpdatedNotificationPayload("9788804336327", "Italo Calvino", "Il barone rampante", "", true, 5, 2, 1);
	}

}
