package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookReservationRejectedV1Handler extends AbstractEventHandler<BookReservationRejectedIntegrationPayload> {

	private final LoanEventReactor reactor;
	
	public BookReservationRejectedV1Handler(LoanEventReactor reactor, EventPayloadMapper mapper) {
		super(mapper);
		this.reactor = reactor;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_RESERVATION_REJECTED;
	}

	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}
	
	@Override
	protected Class<BookReservationRejectedIntegrationPayload> payloadType() {
		return BookReservationRejectedIntegrationPayload.class;
	}
	
	@Override
	protected void process(BookReservationRejectedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		reactor.handleBookReservationRejected(new CommandBookRejectedEvent(payload.loanId(), payload.reason()));
	}

}
