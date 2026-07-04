package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

public class BookReservedV1Handler implements EventHandler {
	
	private final LoanEventReactor reactor;
	private final ObjectMapper mapper;
	
	public BookReservedV1Handler(LoanEventReactor reactor, ObjectMapper mapper) {
		this.reactor = reactor;
		this.mapper = mapper;
	}
	
	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload eventPayload = mapper.convertValue(event.payload(), BookLoanIntegrationPayload.class);
	    reactor.handleBookReserved(new CommandBookEvent(eventPayload.loanId()));
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == IntegrationEventTypes.BOOK_RESERVED && event.schemaVersion() == 1;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_RESERVED;
	}
	
	
}
