package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_REQUESTED, supportedVersions = {1})
@Component
public class LoanRequestedV1Handler extends AbstractEventHandler<LoanRequestedIntegrationPayload> {

	private final BookEventReactor reactor;
		
	public LoanRequestedV1Handler(BookEventReactor reactor, EventPayloadMapper mapper) {
		super(mapper);
		this.reactor = reactor;
	}
	
	@Override
	protected Class<LoanRequestedIntegrationPayload> payloadType() {
		return LoanRequestedIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanRequestedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		CommandLoanEvent command = new CommandLoanEvent(payload.loanId(), payload.isbn(), payload.userId());
		reactor.handleLoanRequested(command);
		return Optional.empty();
	}

}
