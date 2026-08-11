package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.bookservice.application.projection.BookProjectionOperations;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookCopiesUpdatedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class BookCopiesUpdatedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock 
	private BookProjectionOperations projectionOperations;	
	private BookCopiesUpdatedV1Handler handler;
	
	@BeforeEach
	void setUp() {
		handler = new BookCopiesUpdatedV1Handler(projectionOperations, mapper);
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}
	
	@Override
	protected IntegrationEventEnvelope<BookCopiesUpdatedIntegrationPayload> validEvent() {
		return getBookCopiesUpdatedEvent("9788804336327", 5, 1);
	}
	
	@Test
	void shouldHandleBookCopiesUpdatedEvent() {
		IntegrationEventEnvelope<BookCopiesUpdatedIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		BookCopiesUpdatedIntegrationPayload payload = event.payload();
		verify(projectionOperations, times(1)).updateCopies(payload.isbn(), payload.quantity(), event.occurredAt());
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank isbn", "isbn", getBookCopiesUpdatedEvent("", 5, 1)));
	}
	
	private IntegrationEventEnvelope<BookCopiesUpdatedIntegrationPayload> getBookCopiesUpdatedEvent(String isbn, int quantity, int schemaVersion) {
		
		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_COPIES_UPDATED,
				"test-handler", aggregateId, AggregateType.BOOK.name(), 0, Instant.now(),
				schemaVersion, new BookCopiesUpdatedIntegrationPayload(isbn, quantity));
	}

}

