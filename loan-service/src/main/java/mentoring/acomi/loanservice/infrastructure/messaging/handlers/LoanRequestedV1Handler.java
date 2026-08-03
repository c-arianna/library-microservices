package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_REQUESTED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class LoanRequestedV1Handler extends AbstractEventHandler<LoanRequestedIntegrationPayload> {

	private final ProjectionDispatcher dispatcher;
	
	public LoanRequestedV1Handler(@Qualifier("liveDispatcher") ProjectionDispatcher dispatcher, 
			EventPayloadMapper mapper) {
		super(mapper);
		this.dispatcher = dispatcher;
	}

	@Override
	protected Class<LoanRequestedIntegrationPayload> payloadType() {
		return LoanRequestedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanRequestedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		dispatcher.dispatch(event, payload);		
		return Optional.of(new ProjectionUpdateNotification(payload.loanId()));
	}

}
