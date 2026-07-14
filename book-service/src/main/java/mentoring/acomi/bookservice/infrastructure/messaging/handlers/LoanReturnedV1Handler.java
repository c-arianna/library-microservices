package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanReturnedV1Handler extends AbstractEventHandler<LoanIntegrationPayload> {

	private final BookEventReactor reactor;
	
	public LoanReturnedV1Handler(BookEventReactor reactor, EventPayloadMapper mapper) {
		super(mapper);
		this.reactor = reactor;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_RETURNED;
	}

	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}
	
	@Override
	protected Class<LoanIntegrationPayload> payloadType() {
		return LoanIntegrationPayload.class;
	}
	
	@Override
	protected void process(LoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		CommandLoanEvent command = new CommandLoanEvent(payload.loanId(), payload.isbn(), payload.userId());
		reactor.handleLoanReturned(command);
	}
	
}
