package mentoring.acomi.bookservice.messaging.notifications.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.bookservice.application.projection.BookSubscriptionProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.handlers.BookSubscriptionNotifiedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload.BookSubscriptionNotifiedPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;

@ExtendWith(MockitoExtension.class)
public class BookSubscriptionNotifiedV1HandlerTest extends AbstractNotificationHandlerTest {
	
	@Mock
	private BookSubscriptionProjection projection;
	
	private BookSubscriptionNotifiedV1Handler handler;
	
	@BeforeEach
	void setUp() {
		handler = new BookSubscriptionNotifiedV1Handler(projection, mapper);
	}
	
	@Override
	protected NotificationHandler handler() {
		return handler;
	}

	@Override
	protected NotificationEventEnvelope<?> validEvent() {
		return getBookSubscriptionNotifiedEvent(1L, "9788804336327", 1);
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	
	}
	
	private List<InvalidNotificationPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidNotificationPayloadScenario("null subscription id", "subscriptionId", 
	    		           getBookSubscriptionNotifiedEvent(null, "9788804336327", 1)),
	                   new InvalidNotificationPayloadScenario("blank isbn", "isbn",
	                	   getBookSubscriptionNotifiedEvent(1L, "", 1)));
	}
	
	@Test
	void shouldHandleBookSubscriptionNotifiedEvent() {
		NotificationEventEnvelope<BookSubscriptionNotifiedPayload> event = getBookSubscriptionNotifiedEvent(1L, "9788804336327", 1);
		handler.handleEvent(event);
		verify(projection, times(1)).markAsNotified(eq(event.payload().subscriptionId()), any(Instant.class));
	}
			
	private NotificationEventEnvelope<BookSubscriptionNotifiedPayload> getBookSubscriptionNotifiedEvent(Long subscriptionId, String isbn, int schemaVersion) {
		
		return new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED, "test-handler", 
				Instant.now(), schemaVersion, getPayload(subscriptionId, isbn));
	}

	private BookSubscriptionNotifiedPayload getPayload(Long subscriptionId, String isbn) {
		return new BookSubscriptionNotifiedPayload(subscriptionId, isbn);
	}

}
