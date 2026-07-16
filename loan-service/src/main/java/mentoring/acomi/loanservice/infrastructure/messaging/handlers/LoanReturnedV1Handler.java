package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.LoanNotificationService;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@HandlerMetadata(eventType = IntegrationEventTypes.LOAN_RETURNED, supportedVersions = {1})
@Component
public class LoanReturnedV1Handler extends AbstractLoanNotificationHandler<LoanIntegrationPayload> {

	private final LoanProjection projection;
	
	public LoanReturnedV1Handler(LoanProjection projection, EventPayloadMapper mapper, LoanNotificationService notificationService) {
		super(mapper, notificationService);
		this.projection = projection;
	}

	@Override
	protected Class<LoanIntegrationPayload> payloadType() {
		return LoanIntegrationPayload.class;
	}

	@Override
	protected void updateProjection(LoanIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.returnLoan(payload.loanId(), event.occurredAt());
	}

	@Override
	protected String loanId(LoanIntegrationPayload payload) {
		return payload.loanId();
	}

}
