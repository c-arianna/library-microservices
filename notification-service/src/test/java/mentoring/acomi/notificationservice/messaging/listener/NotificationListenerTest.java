package mentoring.acomi.notificationservice.messaging.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.NotificationListener;
import mentoring.acomi.sharedlibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class NotificationListenerTest {

	@Mock
	private EventHandler handler;

	private NotificationListener listener;

	@BeforeEach
	void setUp() {

		when(handler.eventType()).thenReturn(IntegrationEventTypes.BOOK_REGISTERED);

		listener = new NotificationListener(List.of(handler));
	}

	@Test
	void shouldInvokeHandler() {

		IntegrationEventEnvelope<?> event = createBookRegisteredEvent();

		when(handler.accepts(event)).thenReturn(true);

		listener.onEvent(event);

		verify(handler).handleEvent(event);
	}

	@Test
	void shouldIgnoreUnknownEvent() {

		IntegrationEventEnvelope<?> event = createBookBorrowedEvent();

		listener.onEvent(event);

		verify(handler, never()).handleEvent(any());
	}

	@Test
	void shouldIgnoreUnsupportedSchemaVersion() {

		IntegrationEventEnvelope<?> event = createBookRegisteredEvent();

		when(handler.accepts(event)).thenReturn(false);

		listener.onEvent(event);

		verify(handler, never()).handleEvent(any());
	}

	@Test
	void shouldDiscardNotificationHandlingException() {

		IntegrationEventEnvelope<?> event = createBookRegisteredEvent();

		when(handler.accepts(event)).thenReturn(true);

		doThrow(new NotificationHandlingException("invalid payload")).when(handler).handleEvent(event);

		Assertions.assertDoesNotThrow(() -> listener.onEvent(event));
	}

	@Test
	void shouldRethrowUnexpectedException() {

		IntegrationEventEnvelope<?> event = createBookRegisteredEvent();

		when(handler.accepts(event)).thenReturn(true);

		doThrow(new RuntimeException("Error!")).when(handler).handleEvent(event);

		Assertions.assertThrows(RuntimeException.class, () -> listener.onEvent(event));
	}

	private IntegrationEventEnvelope<?> createBookRegisteredEvent() {

		Map<String, String> payload = Map.of("isbn", "9788804336327", "author", "Italo Calvino", "title",
				"Il barono rampante", "description", "");

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_REGISTERED,
				"book-service", "9788804336327", "BOOK", 0, Instant.now(), 1, payload);

	}

	private IntegrationEventEnvelope<?> createBookBorrowedEvent() {

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_BORROWED,
				"book-service", "9788804336327", "BOOK", 0, Instant.now(), 1, Map.of());

	}

}
