package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanRequestedV1Handler implements EventHandler {

	private final BookEventReactor reactor;
	private final EventPayloadMapper mapper;
	
	public LoanRequestedV1Handler(BookEventReactor reactor, EventPayloadMapper mapper) {
		this.reactor = reactor;
		this.mapper = mapper;
	}
	
	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_REQUESTED;
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		LoanRequestedIntegrationPayload payload = mapper.mapAndValidate(event.payload(), LoanRequestedIntegrationPayload.class);
		CommandLoanEvent command = new CommandLoanEvent(payload.loanId(), payload.isbn(), payload.userId());
		reactor.handleLoanRequested(command);
		
	}

}
