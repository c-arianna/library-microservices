package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookBorrowedV1Handler implements EventHandler {
	
	private final LoanEventReactor reactor;
	private final EventPayloadMapper mapper;
	
	public BookBorrowedV1Handler(LoanEventReactor reactor, EventPayloadMapper mapper) {
		this.reactor = reactor;
		this.mapper = mapper;
	}
		
	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload payload = mapper.mapAndValidate(event.payload(), BookLoanIntegrationPayload.class);		
	    reactor.handleBookBorrowed(new CommandBookEvent(payload.loanId()));
	}
	
	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_BORROWED;
	}
	
}