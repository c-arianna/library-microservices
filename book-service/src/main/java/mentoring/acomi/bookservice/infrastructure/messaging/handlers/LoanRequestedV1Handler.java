package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanRequestedV1Handler extends AbstractEventHandler<LoanRequestedIntegrationPayload> {

	private final BookEventReactor reactor;
		
	public LoanRequestedV1Handler(BookEventReactor reactor, EventPayloadMapper mapper) {
		super(mapper);
		this.reactor = reactor;
	}
	
	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_REQUESTED;
	}

	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}
	
	@Override
	protected Class<LoanRequestedIntegrationPayload> payloadType() {
		return LoanRequestedIntegrationPayload.class;
	}
	
	@Override
	protected void process(LoanRequestedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		CommandLoanEvent command = new CommandLoanEvent(payload.loanId(), payload.isbn(), payload.userId());
		reactor.handleLoanRequested(command);
	}

}
