package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanReturnedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_RETURNED, supportedVersions = {1,2}, mode = HandlerMode.REPLAYABLE)
@Component
public class LoanReturnedHandler extends AbstractEventHandler<LoanReturnedIntegrationPayload> {

	private final ProjectionDispatcher dispatcher;
	
	public LoanReturnedHandler(@Qualifier("liveDispatcher") ProjectionDispatcher dispatcher, EventPayloadMapper mapper) {
		super(mapper);
		this.dispatcher = dispatcher;
	}

	@Override
	protected Class<LoanReturnedIntegrationPayload> payloadType() {
		return LoanReturnedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanReturnedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		
		if (event.schemaVersion() >= 2 && payload.returnedAt() == null) {
				throw new IllegalStateException("invalid field returnedAt for schema version 2");
		}

		dispatcher.dispatch(event, payload);
		return Optional.of(new ProjectionUpdateNotification(payload.loanId()));
	}

}
