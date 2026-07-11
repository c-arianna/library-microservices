package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.LoanProjection;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.LoanNotificationService;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.producer.LoanFailedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class LoanFailedV1Handler implements EventHandler {

	private final LoanProjection projection;
	private final EventPayloadMapper mapper;
	private final LoanNotificationService notificationService;
	
	public LoanFailedV1Handler(LoanProjection projection, EventPayloadMapper mapper, LoanNotificationService notificationService) {
		this.projection = projection;
		this.mapper = mapper;
		this.notificationService = notificationService;
	}

	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.LOAN_FAILED;
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		LoanFailedIntegrationPayload payload = mapper.mapAndValidate(event.payload(), LoanFailedIntegrationPayload.class);
		projection.failLoan(payload.loanId(), event.occurredAt());
		notificationService.publishLoanUpdated(payload.loanId(), event.schemaVersion());
	}

}
