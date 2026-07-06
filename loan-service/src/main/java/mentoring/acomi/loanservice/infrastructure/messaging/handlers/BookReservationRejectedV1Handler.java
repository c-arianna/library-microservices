package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.reactor.LoanEventReactor;
import mentoring.acomi.loanservice.application.reactor.command.CommandBookRejectedEvent;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookReservationRejectedV1Handler implements EventHandler {

	private final LoanEventReactor reactor;
	private final EventPayloadMapper mapper;

	public BookReservationRejectedV1Handler(LoanEventReactor reactor, EventPayloadMapper mapper) {
		this.reactor = reactor;
		this.mapper = mapper;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_RESERVATION_REJECTED;
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		BookReservationRejectedIntegrationPayload payload = mapper.mapAndValidate(event.payload(), BookReservationRejectedIntegrationPayload.class);
		reactor.handleBookReservationRejected(new CommandBookRejectedEvent(payload.loanId(), payload.reason()));

	}

}
