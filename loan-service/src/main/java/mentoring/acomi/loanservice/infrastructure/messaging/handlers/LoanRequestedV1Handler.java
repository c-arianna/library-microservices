package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.LoanNotificationService;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanRequestedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanRequestedV1Handler extends AbstractLoanNotificationHandler<LoanRequestedIntegrationPayload> {

	private final LoanProjection projection;
		
	public LoanRequestedV1Handler(LoanProjection projection, EventPayloadMapper mapper, LoanNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_REQUESTED;
	}

	@Override
	protected Class<LoanRequestedIntegrationPayload> payloadType() {
		return LoanRequestedIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(LoanRequestedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.loanInsert(payload, event.occurredAt());
	}

	@Override
	protected String loanId(LoanRequestedIntegrationPayload payload) {
		return payload.loanId();
	}

}
