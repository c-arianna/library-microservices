package mentoring.acomi.bookservice.messaging.notifications.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.bookservice.application.projection.BookSubscriptionProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.handlers.BookSubscriptionNotifiedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload.BookSubscriptionNotifiedPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BookSubscriptionNotifiedV1HandlerTest {

	private ObjectMapper mapper = new ObjectMapper();;
	
	@Mock
	private BookSubscriptionProjection projection;
	
	private BookSubscriptionNotifiedV1Handler handler;
	
	@BeforeEach
	void setUp() {
		handler = new BookSubscriptionNotifiedV1Handler(projection, mapper);
	}
	
	@Test
	void shouldHandleBookSubscriptionNotifiedEvent() {
		NotificationEventEnvelope<BookSubscriptionNotifiedPayload> event = getBookSubscriptionNotifiedEvent(1L, "9788804336327", 1);
		handler.handleEvent(event);
		verify(projection, times(1)).markAsNotified(eq(event.payload().subscriptionId()), any(Instant.class));
	}
	
	@Test
	void shouldRejectNullPayload() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED, "test-handler", Instant.now(), 1, null);

		NotificationHandlingException ex  = Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));
		Assertions.assertEquals("Payload is null", ex.getMessage());
		
		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithNullSubscriptionId() {

		NotificationEventEnvelope<?> event = getBookSubscriptionNotifiedEvent(null, "9788804336327", 1);

		NotificationHandlingException ex  = Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));
		Assertions.assertEquals("Missing required field 'subscriptionId'", ex.getMessage());
		
		verifyNoInteractions(projection);
	}

	@Test
	void shouldRejectPayloadWithBlankIsbn() {

		NotificationEventEnvelope<?> event = getBookSubscriptionNotifiedEvent(1L, "", 1);

		NotificationHandlingException ex  = Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));
		Assertions.assertEquals("Blank required field 'isbn'", ex.getMessage());
		
		verifyNoInteractions(projection);
	}

	
	private NotificationEventEnvelope<BookSubscriptionNotifiedPayload> getBookSubscriptionNotifiedEvent(Long subscriptionId, String isbn, int schemaVersion) {
		
		return new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED, "test-handler", 
				Instant.now(), schemaVersion, getPayload(subscriptionId, isbn));
	}

	private BookSubscriptionNotifiedPayload getPayload(Long subscriptionId, String isbn) {
		return new BookSubscriptionNotifiedPayload(subscriptionId, isbn);
	}
}
