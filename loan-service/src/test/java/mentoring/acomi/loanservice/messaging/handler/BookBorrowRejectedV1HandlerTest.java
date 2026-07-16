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
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.BookBorrowRejectedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@ExtendWith(MockitoExtension.class)
public class BookBorrowRejectedV1HandlerTest extends AbstractEventHandlerTest {

	private static final String RESERVATION_MISSING = "RESERVATION_MISSING";

	@Mock
    private LoanEventReactor reactor;
    private BookBorrowRejectedV1Handler handler;

    @BeforeEach
    void setUp() {
        handler = new BookBorrowRejectedV1Handler(reactor, mapper);
    }

    @Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<BookBorrowRejectedIntegrationPayload> validEvent() {
		return getBookBorrowRejectedEvent("9788804336327", UUID.randomUUID().toString(), UUID.randomUUID().toString(), RESERVATION_MISSING, 1);
	}
	
    @Test
    void shouldHandleBookBorrowRejectedEvent() {
        IntegrationEventEnvelope<BookBorrowRejectedIntegrationPayload> event = validEvent();
        handler.handleEvent(event);

        CommandBookRejectedEvent command = new CommandBookRejectedEvent(event.payload().loanId(), event.payload().reason());
        verify(reactor, times(1)).handleBookBorrowRejected(command);
    }

    @TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), reactor))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank isbn", "isbn",
	    		          getBookBorrowRejectedEvent("", UUID.randomUUID().toString(), UUID.randomUUID().toString(), RESERVATION_MISSING, 1)),
	    		       new InvalidPayloadScenario("blank loanId", "loanId", getBookBorrowRejectedEvent("9788804336327", "", UUID.randomUUID().toString(), 
	    		    		   RESERVATION_MISSING, 1)),
	    		       new InvalidPayloadScenario("blank userId", "userId", getBookBorrowRejectedEvent("9788804336327", UUID.randomUUID().toString(), "", 
	    		    		   RESERVATION_MISSING, 1)),
	    		       new InvalidPayloadScenario("blank reason", "reason", getBookBorrowRejectedEvent("9788804336327", UUID.randomUUID().toString(), 
				    		     UUID.randomUUID().toString(), "", 1)));
	}
	        	
	private IntegrationEventEnvelope<BookBorrowRejectedIntegrationPayload> getBookBorrowRejectedEvent(String isbn, String loanId, 
			String userId, String reason, int schemaVersion) {

		String aggregateId = isbn == null || isbn.isBlank() ? "9788804336327" : isbn;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.BOOK_BORROW_REJECTED, "test-handler", aggregateId,
	                AggregateType.BOOK.name(), 0, Instant.now(), schemaVersion, 
	                new BookBorrowRejectedIntegrationPayload(isbn, loanId, userId, reason));
	}
}
