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
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookCopiesUpdatedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BookCopiesUpdateV1HandlerTest extends AbstractEventHandlerTest {

	@Mock 
	private BookProjection projection;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;
	
	private BookCopiesUpdatedV1Handler handler;
	
	@BeforeEach
	void setUp() {
		mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new BookCopiesUpdatedV1Handler(projection, mapper);
	}
	
	@Test
	void shouldHandleBookCopiesUpdatedEvent() {
		
		IntegrationEventEnvelope<BookCopiesUpdatedIntegrationPayload> event = validEvent();
		
		handler.handleEvent(event);
		
		verify(projection, times(1)).updateCopies(event.payload(), event.occurredAt());
		
	}
	
	@Test
	void shouldRejectPayloadWithBlankIsbn() {
	
		IntegrationEventEnvelope<BookCopiesUpdatedIntegrationPayload> event = getBookCopiesUpdatedEvent("", 5, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("isbn"),
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
	protected IntegrationEventEnvelope<BookCopiesUpdatedIntegrationPayload> validEvent() {
		return getBookCopiesUpdatedEvent("9788804336327", 5, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LOAN_CONFIRM_REQUESTED,
				"loan-service", "9788804336327", AggregateType.LOAN.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getBookCopiesUpdatedEvent("9788804336327", 10, schemaVersion);
	}
	
	private IntegrationEventEnvelope<BookCopiesUpdatedIntegrationPayload> getBookCopiesUpdatedEvent(String isbn, int quantity, int schemaVersion) {
		
		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_COPIES_UPDATED,
				"test-handler", aggregateId, AggregateType.BOOK.name(), 0, Instant.now(),
				schemaVersion, new BookCopiesUpdatedIntegrationPayload(isbn, quantity));
	}

}

