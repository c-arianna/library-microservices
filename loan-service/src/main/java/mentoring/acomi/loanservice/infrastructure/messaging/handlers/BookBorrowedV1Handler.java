package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookBorrowedV1Handler extends AbstractEventHandler<BookLoanIntegrationPayload> {
	
	private final LoanEventReactor reactor;
	
	public BookBorrowedV1Handler(LoanEventReactor reactor, EventPayloadMapper mapper) {
		super(mapper);
		this.reactor = reactor;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_BORROWED;
	}
	
	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}
	
	@Override
	protected Class<BookLoanIntegrationPayload> payloadType() {
		return BookLoanIntegrationPayload.class;
	}
	
	@Override
	protected void process(BookLoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		reactor.handleBookBorrowed(new CommandBookEvent(payload.loanId()));
	}

}