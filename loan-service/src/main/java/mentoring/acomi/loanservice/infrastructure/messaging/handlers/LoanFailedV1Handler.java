package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_FAILED, supportedVersions = {1})
@Component
public class LoanFailedV1Handler extends AbstractEventHandler<LoanFailedIntegrationPayload> {

	private final LoanProjection projection;
		
	public LoanFailedV1Handler(LoanProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}

	@Override
	protected Class<LoanFailedIntegrationPayload> payloadType() {
		return LoanFailedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanFailedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.failLoan(payload.loanId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.loanId(), event.schemaVersion()));
	}

}
