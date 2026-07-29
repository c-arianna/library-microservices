package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjectionOperations;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_FAILED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class LoanFailedV1Handler extends AbstractEventHandler<LoanFailedIntegrationPayload> {

	private final LoanProjectionOperations projectionOperations;
		
	public LoanFailedV1Handler(@Qualifier("liveLoanProjection") LoanProjectionOperations projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}

	@Override
	protected Class<LoanFailedIntegrationPayload> payloadType() {
		return LoanFailedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanFailedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.failLoan(payload.loanId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.loanId()));
	}

}
