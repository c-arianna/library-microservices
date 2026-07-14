package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookBorrowRejectedV1Handler extends AbstractEventHandler<BookBorrowRejectedIntegrationPayload> {

	private final LoanEventReactor reactor;
	
	public BookBorrowRejectedV1Handler(LoanEventReactor reactor, EventPayloadMapper mapper) {
		super(mapper);
		this.reactor = reactor;
	}
		
	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_BORROW_REJECTED;
	}

	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}
	
	@Override
	protected Class<BookBorrowRejectedIntegrationPayload> payloadType() {
		return BookBorrowRejectedIntegrationPayload.class;
	}
	
	@Override
	protected void process(BookBorrowRejectedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		reactor.handleBookBorrowRejected(new CommandBookRejectedEvent(payload.loanId(), payload.reason()));
	}
	
}
