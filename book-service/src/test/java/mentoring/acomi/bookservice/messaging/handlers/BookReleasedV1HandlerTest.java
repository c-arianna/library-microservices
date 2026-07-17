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
import mentoring.acomi.bookservice.infrastructure.messaging.handlers.BookReleasedV1Handler;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class BookReleasedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private BookProjectionOperations projectionOperations;
	private BookReleasedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new BookReleasedV1Handler(projectionOperations, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<BookLoanIntegrationPayload> validEvent() {
		return getBookReleasedEvent("9788804336327", UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1);
	}

	@Test
	void shouldHandleBookReleasedEvent() {
		IntegrationEventEnvelope<BookLoanIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		verify(projectionOperations, times(1)).release(event.payload(), event.occurredAt());

	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank isbn", "isbn",  
	    		           getBookReleasedEvent("", UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank loanId", "loanId", getBookReleasedEvent("9788804336327", "", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank userId", "userId", getBookReleasedEvent("9788804336327", UUID.randomUUID().toString(), "", 1)));
	}
	
	private IntegrationEventEnvelope<BookLoanIntegrationPayload> getBookReleasedEvent(String isbn, String loanId,
			String userId, int schemaVersion) {

		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_RELEASED,
				"test-handler", aggregateId, AggregateType.BOOK.name(), 0, Instant.now(), schemaVersion,
				new BookLoanIntegrationPayload(isbn, loanId, userId));
	}

}
