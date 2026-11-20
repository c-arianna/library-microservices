package mentoring.acomi.assistantservice.messaging.notifications.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

import mentoring.acomi.assistantservice.application.projection.BookEmbeddingProjection;
import mentoring.acomi.assistantservice.application.projection.ProjectionOperations;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.handlers.BookUpdatedV1NotificationHandler;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.BookUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;

@ExtendWith(MockitoExtension.class)
public class BookUpdatedV1NotificationHandlerTest  extends AbstractNotificationHandlerTest {

	private static final String TITLE = "Il barone rampante";

	private static final String AUTHOR = "Italo Calvino";

	private static final String ISBN = "9788804336327";

	@Mock
	private ProjectionOperations<BookView> bookProjection;
	
	@Mock
	private BookEmbeddingProjection embeddingProjection;
	
	private BookUpdatedV1NotificationHandler handler;
	
	@BeforeEach
	void setUp() {
		handler = new BookUpdatedV1NotificationHandler(bookProjection, embeddingProjection, mapper);
	}
	
	@Override
	protected NotificationHandler handler() {
		return handler;
	}

	@Override
	protected NotificationEventEnvelope<BookUpdatedNotificationPayload> validEvent() {
		return getBookUpdatedEvent(ISBN, AUTHOR, TITLE, "", 1);
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), bookProjection))).toList();
	
	}
	
	private List<InvalidNotificationPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidNotificationPayloadScenario("blank isbn", "isbn",
	                	   getBookUpdatedEvent("", AUTHOR, TITLE, "", 1)),
	    		       new InvalidNotificationPayloadScenario("blank author", "author",
	                	   getBookUpdatedEvent(ISBN, "", TITLE, "", 1)),
	    		       new InvalidNotificationPayloadScenario("blank title", "title",
		                	   getBookUpdatedEvent(ISBN, AUTHOR, "", "", 1)),
	    		       new InvalidNotificationPayloadScenario("null description", "description",
		                	   getBookUpdatedEvent(ISBN, AUTHOR, TITLE, null, 1)));
	}
	
	@Test
	void shouldHandleBookSubscriptionNotifiedEvent() {
		NotificationEventEnvelope<BookUpdatedNotificationPayload> event = validEvent();
		handler.handleEvent(event);
		BookView book = handler.getBook(event.payload());
		verify(bookProjection, times(1)).upsert(eq(book), any(Instant.class));
		verify(embeddingProjection, times(1)).index(book);
	}
			
	private NotificationEventEnvelope<BookUpdatedNotificationPayload> getBookUpdatedEvent(String isbn, String author, String title, 
			String description, int schemaVersion) {
		
		return new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.BOOK_UPDATED, "test-handler", 
				Instant.now(), schemaVersion, getPayload(isbn, author, title, description));
	}

	private BookUpdatedNotificationPayload getPayload(String isbn, String author, String title, String description) {
		return new BookUpdatedNotificationPayload(isbn, author, title, description, false, 0, 0, 0);
	}
		
}
