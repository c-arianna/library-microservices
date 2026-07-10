package mentoring.acomi.notificationservice.messaging.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.NotificationListener;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@ExtendWith(MockitoExtension.class)
public class NotificationListenerTest {

	@Mock
	private NotificationHandler handler;

	private NotificationListener listener;

	@BeforeEach
	void setUp() {

		when(handler.eventType()).thenReturn(NotificationEventType.BOOK_UPDATED);

		listener = new NotificationListener(List.of(handler));
	}

	@Test
	void shouldInvokeHandler() {

		NotificationEventEnvelope<?> event = buildBookUpdatedEvent();

		when(handler.accepts(event)).thenReturn(true);

		listener.onEvent(event);

		verify(handler).handleEvent(event);
	}

	@Test
	void shouldIgnoreUnknownEvent() {

		NotificationEventEnvelope<?> event = buildBookUpdatedEvent();

		listener.onEvent(event);

		verify(handler, never()).handleEvent(any());
	}

	@Test
	void shouldIgnoreUnsupportedSchemaVersion() {

		NotificationEventEnvelope<?> event = buildBookUpdatedEvent();

		when(handler.accepts(event)).thenReturn(false);

		listener.onEvent(event);

		verify(handler, never()).handleEvent(any());
	}

	@Test
	void shouldDiscardNotificationHandlingException() {

		NotificationEventEnvelope<?> event = buildBookUpdatedEvent();

		when(handler.accepts(event)).thenReturn(true);

		doThrow(new NotificationHandlingException("invalid payload")).when(handler).handleEvent(event);

		Assertions.assertDoesNotThrow(() -> listener.onEvent(event));
	}

	@Test
	void shouldRethrowUnexpectedException() {

		NotificationEventEnvelope<?> event = buildBookUpdatedEvent();

		when(handler.accepts(event)).thenReturn(true);

		doThrow(new RuntimeException("Error!")).when(handler).handleEvent(event);

		Assertions.assertThrows(RuntimeException.class, () -> listener.onEvent(event));
	}

	private NotificationEventEnvelope<BookUpdatedNotificationPayload> buildBookUpdatedEvent() {
		return new NotificationEventEnvelope<BookUpdatedNotificationPayload>(UUID.randomUUID().toString(), 
				NotificationEventType.BOOK_UPDATED, "book-service", Instant.now(), 1, getPayload());
	}

	private BookUpdatedNotificationPayload getPayload() {
		return new BookUpdatedNotificationPayload("9788804336327", "Italo Calvino", "Il barone rampante", "", true, 5, 2, 1);
	}

}
