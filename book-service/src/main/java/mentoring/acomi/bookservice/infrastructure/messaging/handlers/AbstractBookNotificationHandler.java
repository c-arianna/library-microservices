package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;

public abstract class AbstractBookNotificationHandler<T> extends AbstractEventHandler<T> {

	private final BookNotificationService notificationService;

	protected AbstractBookNotificationHandler(EventPayloadMapper mapper, BookNotificationService notificationService) {
		super(mapper);
		this.notificationService = notificationService;
	}

	@Override
	protected final void process(T payload, IntegrationEventEnvelope<?> event) {
		updateProjection(payload, event);
		notificationService.publishBookUpdated(isbn(payload), event.schemaVersion());
	}

	protected abstract void updateProjection(T payload, IntegrationEventEnvelope<?> event);

	protected abstract String isbn(T payload);
}
