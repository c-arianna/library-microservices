package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_REQUESTED, supportedVersions = {1})
@Component
public class LoanRequestedV1Handler extends AbstractEventHandler<LoanRequestedIntegrationPayload> {

	private final LoanProjection projection;
		
	public LoanRequestedV1Handler(LoanProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}

	@Override
	protected Class<LoanRequestedIntegrationPayload> payloadType() {
		return LoanRequestedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(LoanRequestedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.loanInsert(payload, event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.loanId(), event.schemaVersion()));
	}

}
