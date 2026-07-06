package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanCanceledV1Handler implements EventHandler {

	private final LoanProjection projection;
	private final EventPayloadMapper mapper;
	
	public LoanCanceledV1Handler(LoanProjection projection, EventPayloadMapper mapper) {
		this.projection = projection;
		this.mapper = mapper;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_CANCELED;
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		LoanIntegrationPayload payload = mapper.mapAndValidate(event.payload(), LoanIntegrationPayload.class);
		projection.cancelLoan(payload.loanId(), event.occurredAt());
		
	}

}
