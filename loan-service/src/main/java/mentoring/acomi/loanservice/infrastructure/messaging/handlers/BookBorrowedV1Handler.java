package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookBorrowedV1Handler implements EventHandler{
	
	private final LoanEventReactor reactor;
	private final ObjectMapper mapper;
	
	public BookBorrowedV1Handler(LoanEventReactor reactor, ObjectMapper mapper) {
		this.reactor = reactor;
		this.mapper = mapper;
	}
	
	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload eventPayload = mapper.convertValue(event.payload(), BookLoanIntegrationPayload.class);
	    reactor.handleBookBorrowed(new CommandBookEvent(eventPayload.loanId()));
	}
	
	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == IntegrationEventTypes.BOOK_BORROWED && event.schemaVersion() == 1;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_BORROWED;
	}
	
}