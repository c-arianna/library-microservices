package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_CONFIRMED, supportedVersions = {1})
@Component
public class LoanConfirmedV1Handler extends AbstractEventHandler<LoanIntegrationPayload> {

	private final LoanProjection projection;
		
	public LoanConfirmedV1Handler(LoanProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}
	
	@Override
	protected Class<LoanIntegrationPayload> payloadType() {
		return LoanIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.confirmLoan(payload.loanId(), event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.loanId(), event.schemaVersion()));
	}

}
