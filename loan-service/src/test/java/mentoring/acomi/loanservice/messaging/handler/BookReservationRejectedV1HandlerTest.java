package mentoring.acomi.loanservice.messaging.handler;

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

import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.BookReservationRejectedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class BookReservationRejectedV1HandlerTest extends AbstractEventHandlerTest {

	private static final String BOOK_NOT_AVAILABLE = "BOOK_NOT_AVAILABLE";

	@Mock
    private LoanEventReactor reactor;
    private BookReservationRejectedV1Handler handler;

    @BeforeEach
    void setUp() {
        handler = new BookReservationRejectedV1Handler(reactor, mapper);
    }
    
    @Override
	protected EventHandler handler() {
		return handler;
	}
    
	@Override
	protected IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> validEvent() {
		return getBookReservationRejectedEvent("9788804336327", UUID.randomUUID().toString(), UUID.randomUUID().toString(), BOOK_NOT_AVAILABLE, 1);
	}

    @Test
    void shouldHandleBookReservationRejectedEvent() {
        IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> event = validEvent();
        handler.handleEvent(event);

        CommandBookRejectedEvent command = new CommandBookRejectedEvent(event.payload().loanId(), event.payload().reason());
        verify(reactor, times(1)).handleBookReservationRejected(command);
    }

    @TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), reactor))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank isbn", "isbn", 
	    		             getBookReservationRejectedEvent("", UUID.randomUUID().toString(), UUID.randomUUID().toString(), BOOK_NOT_AVAILABLE, 1)),
	                   new InvalidPayloadScenario("blank loanId", "loanId", 
			                 getBookReservationRejectedEvent("9788804336327", "", UUID.randomUUID().toString(), BOOK_NOT_AVAILABLE, 1)),
	                   new InvalidPayloadScenario("blank userId", "userId",
	                		   getBookReservationRejectedEvent("9788804336327", UUID.randomUUID().toString(), "", BOOK_NOT_AVAILABLE, 1)),
	                   new InvalidPayloadScenario("blank reason", "reason", 
	                		   getBookReservationRejectedEvent("9788804336327", UUID.randomUUID().toString(), UUID.randomUUID().toString(), "", 1)));
	}
	    
    private IntegrationEventEnvelope<BookReservationRejectedIntegrationPayload> getBookReservationRejectedEvent(String isbn, String loanId, 
			String userId, String reason, int schemaVersion) {

		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_RESERVATION_REJECTED, "test-handler", 
				aggregateId, AggregateType.BOOK.name(), 0, Instant.now(), schemaVersion, 
				new BookReservationRejectedIntegrationPayload(isbn, loanId, userId, reason));
	}
}
