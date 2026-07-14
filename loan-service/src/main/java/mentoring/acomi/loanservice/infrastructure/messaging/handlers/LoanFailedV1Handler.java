package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.LoanNotificationService;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanFailedV1Handler extends AbstractLoanNotificationHandler<LoanFailedIntegrationPayload> {

	private final LoanProjection projection;
		
	public LoanFailedV1Handler(LoanProjection projection, EventPayloadMapper mapper, LoanNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_FAILED;
	}
	
	@Override
	protected int supportedSchemaVersion() {
		return 1;
	}
	
	@Override
	protected Class<LoanFailedIntegrationPayload> payloadType() {
		return LoanFailedIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(LoanFailedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.failLoan(payload.loanId(), event.occurredAt());
	}

	@Override
	protected String loanId(LoanFailedIntegrationPayload payload) {
		return payload.loanId();
	}

}
