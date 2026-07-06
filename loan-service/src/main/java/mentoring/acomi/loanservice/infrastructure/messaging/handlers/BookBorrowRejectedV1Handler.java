package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookBorrowRejectedV1Handler implements EventHandler {

	private final LoanEventReactor reactor;
	private final EventPayloadMapper mapper;

	public BookBorrowRejectedV1Handler(LoanEventReactor reactor, EventPayloadMapper mapper) {
		this.reactor = reactor;
		this.mapper = mapper;
	}
		
	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_BORROW_REJECTED;
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		BookBorrowRejectedIntegrationPayload payload = mapper.mapAndValidate(event.payload(), BookBorrowRejectedIntegrationPayload.class);
		reactor.handleBookBorrowRejected(new CommandBookRejectedEvent(payload.loanId(), payload.reason()));
	}
	
}
