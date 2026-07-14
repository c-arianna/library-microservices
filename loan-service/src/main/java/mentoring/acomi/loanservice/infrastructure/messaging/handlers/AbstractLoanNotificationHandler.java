package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import mentoring.acomi.loanservice.infrastructure.messaging.notifications.LoanNotificationService;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public abstract class AbstractLoanNotificationHandler<T> extends AbstractEventHandler<T> {
	
	private LoanNotificationService notificationService;

	protected AbstractLoanNotificationHandler(EventPayloadMapper mapper, LoanNotificationService notificationService) {
		super(mapper);
		this.notificationService = notificationService;
	}

	@Override
	protected final void process(T payload, IntegrationEventEnvelope<?> event) {
		updateProjection(payload, event);
		notificationService.publishLoanUpdated(loanId(payload), event.schemaVersion());
	}

	protected abstract void updateProjection(T payload, IntegrationEventEnvelope<?> event);

	protected abstract String loanId(T payload);
}
