package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.reactor.BookEventReactor;
import mentoring.acomi.bookservice.application.reactor.command.CommandLoanEvent;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanReturnedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_RETURNED, supportedVersions = {1,2}, mode = HandlerMode.LIVE_ONLY)
@Component
public class LoanReturnedHandler extends AbstractEventHandler<LoanReturnedIntegrationPayload> {

	private final BookEventReactor reactor;
	
	public LoanReturnedHandler(BookEventReactor reactor, EventPayloadMapper mapper) {
		super(mapper);
		this.reactor = reactor;
	}
	
	@Override
	protected Class<LoanReturnedIntegrationPayload> payloadType() {
		return LoanReturnedIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanReturnedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		CommandLoanEvent command = new CommandLoanEvent(payload.loanId(), payload.isbn(), payload.userId());
		reactor.handleLoanReturned(command);
		return Optional.empty();
	}
	
}
