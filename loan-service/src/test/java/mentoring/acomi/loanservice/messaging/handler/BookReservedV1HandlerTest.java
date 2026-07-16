package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.verify;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.BookReservedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class BookReservedV1HandlerTest extends AbstractEventHandlerTest {

    @Mock
    private LoanEventReactor reactor;
    private BookReservedV1Handler handler;

    @BeforeEach
    void setUp() {
        handler = new BookReservedV1Handler(reactor, mapper);
    }

    @Override
    protected EventHandler handler() {
        return handler;
    }

    @Override
    protected IntegrationEventEnvelope<BookLoanIntegrationPayload> validEvent() {
        return getBookReservedEvent("9788804336327", UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1);
    }
    
    @Test
    void shouldHandleBookReservedEvent() {
        IntegrationEventEnvelope<BookLoanIntegrationPayload> event = validEvent();
        Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);

        Assertions.assertTrue(notification.isEmpty());
        
        CommandBookEvent command = new CommandBookEvent(event.payload().loanId());
        verify(reactor, times(1)).handleBookReserved(command);
    }

    @TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), reactor))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank isbn", "isbn", 
	    		            getBookReservedEvent("", UUID.randomUUID().toString(), UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank loanId", "loanId", 
	                		getBookReservedEvent("9788804336327", "", UUID.randomUUID().toString(), 1)),
	                   new InvalidPayloadScenario("blank userId", "userId",
	                		   getBookReservedEvent("9788804336327", UUID.randomUUID().toString(), "", 1)));
	}
	
    private IntegrationEventEnvelope<BookLoanIntegrationPayload> getBookReservedEvent(String isbn, String loanId, String userId, int schemaVersion) {

    	String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
    	
        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_RESERVED, "test-handler", aggregateId,
                AggregateType.BOOK.name(), 0, Instant.now(), schemaVersion, new BookLoanIntegrationPayload(isbn, loanId, userId));
    }
}