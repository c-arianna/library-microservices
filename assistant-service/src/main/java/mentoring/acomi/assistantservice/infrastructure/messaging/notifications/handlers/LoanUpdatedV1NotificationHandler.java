package mentoring.acomi.assistantservice.infrastructure.messaging.notifications.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.projection.ProjectionOperations;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.LoanUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.LOAN_UPDATED, supportedVersions = {1})
@Component
public class LoanUpdatedV1NotificationHandler extends AbstractNotificationHandler<LoanUpdatedNotificationPayload> {

	private final ProjectionOperations<ReaderHistoryView> projectionOperations;
	
	public LoanUpdatedV1NotificationHandler(ProjectionOperations<ReaderHistoryView> projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}

	@Override
	protected Class<LoanUpdatedNotificationPayload> payloadType() {
		return LoanUpdatedNotificationPayload.class;
	}

	@Override
	protected void process(LoanUpdatedNotificationPayload payload, NotificationEventEnvelope<?> event) {
		ReaderHistoryView view = getView(payload);
		projectionOperations.upsert(view, event.occurredAt());		
	}

	public ReaderHistoryView getView(LoanUpdatedNotificationPayload payload) {
		return new ReaderHistoryView(payload.loanId(), payload.userId(), payload.isbn(), payload.startDate(), payload.endDate(), 
				payload.status());
	}

}
