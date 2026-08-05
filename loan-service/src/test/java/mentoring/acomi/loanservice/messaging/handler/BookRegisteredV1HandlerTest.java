package mentoring.acomi.loanservice.messaging.handler;

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

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.BookRegisteredV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookRegisteredIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@ExtendWith(MockitoExtension.class)
public class BookRegisteredV1HandlerTest extends AbstractEventHandlerTest{

	@Mock
	private ProjectionDispatcher dispatcher;
	
	private BookRegisteredV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new BookRegisteredV1Handler(dispatcher, mapper);
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<BookRegisteredIntegrationPayload> validEvent() {
		return getBookRegisteredEvent("9788804336327", "Italo Calvino", "Il visconte dimezzato", "", 1);
	}
	
	@Test
	void shouldHandleBookRegisteredEvent() {
		IntegrationEventEnvelope<BookRegisteredIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isEmpty());
		verify(dispatcher).dispatch(event, event.payload());	
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), dispatcher))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank isbn", "isbn", 
	    					   getBookRegisteredEvent("", "Italo Calvino", "Il visconte dimezzato", "", 1)),
	                   new InvalidPayloadScenario("blank author", "author", 
	                		   getBookRegisteredEvent("9788804336327", "", "Il visconte dimezzato", "", 1)),
	                   new InvalidPayloadScenario("blank title", "title", getBookRegisteredEvent("9788804336327", "Italo Calvino", "", "", 1)),
	                   new InvalidPayloadScenario("null description", "description", 
	                		   getBookRegisteredEvent("9788804336327", "Italo Calvino", "Il visconte dimezzato", null, 1)));                  
	}
		
	private IntegrationEventEnvelope<BookRegisteredIntegrationPayload> getBookRegisteredEvent(String isbn, String author, String title,
			String description, int schemaVersion) {

		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_REGISTERED,
			"test-handler", aggregateId, AggregateType.BOOK.name(), 0, Instant.now(), schemaVersion, 
			new BookRegisteredIntegrationPayload(isbn, author, title, description));
	}

}
