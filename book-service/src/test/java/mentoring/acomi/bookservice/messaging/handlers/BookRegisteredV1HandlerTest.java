package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.InvalidEventPayloadException;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookRegisteredV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BookRegisteredV1HandlerTest extends AbstractEventHandlerTest {

	@Mock 
	private BookProjection projection;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;
	
	@Mock
	private BookNotificationService notificationService;
	
	private BookRegisteredV1Handler handler;
	
	@BeforeEach
	void setUp() {
		mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new BookRegisteredV1Handler(projection, mapper, notificationService);
	}
	
	@Test
	void shouldHandleBookRegisteredEvent() {
		
		IntegrationEventEnvelope<BookRegisteredIntegrationPayload> event = validEvent();
		
		handler.handleEvent(event);
		
		verify(projection, times(1)).addBook(event.payload(), event.occurredAt());
		
	}
	
	@Test
	void shouldRejectPayloadWithBlankIsbn() {
	
		IntegrationEventEnvelope<BookRegisteredIntegrationPayload> event = getBookRegisteredEvent("", "Italo Calvino", "Il barone rampante", 
				"Appartiene a una trilogia", 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("isbn"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithBlankAuthor() {
	
		IntegrationEventEnvelope<BookRegisteredIntegrationPayload> event = getBookRegisteredEvent("9788804336327", "", "Il barone rampante", 
				"Appartiene a una trilogia", 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("author"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithBlankTitle() {
	
		IntegrationEventEnvelope<BookRegisteredIntegrationPayload> event = getBookRegisteredEvent("9788804336327", "Italo Calvino", "", 
				"Appartiene a una trilogia", 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("title"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithoutDescription() {
	
		IntegrationEventEnvelope<BookRegisteredIntegrationPayload> event = getBookRegisteredEvent("9788804336327", "Italo Calvino", 
				"Il barone rampante", null, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("description"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventTypes eventType() {
		return handler.eventType();
	}

	@Override
	protected IntegrationEventEnvelope<BookRegisteredIntegrationPayload> validEvent() {
		return getBookRegisteredEvent("9788804336327", "Italo Calvino", "Il barone rampante", "Appartiene a una trilogia", 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CONFIRM_REQUESTED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getBookRegisteredEvent("9788804336327", "Italo Calvino", "Il barone rampante", "Appartiene a una trilogia", schemaVersion);
	}
	
	private IntegrationEventEnvelope<BookRegisteredIntegrationPayload> getBookRegisteredEvent(String isbn, String author, String title, 
			String description, int schemaVersion) {
		
		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_REGISTERED,
				"test-handler", aggregateId, AggregateType.BOOK.name(), 0, Instant.now(),
				schemaVersion, new BookRegisteredIntegrationPayload(isbn, author, title, description));
	}

}
