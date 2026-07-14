package mentoring.acomi.userservice.infrastructure.messaging.handlers;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.UserNotificationService;

public abstract class AbstractUserNotificationHandler<T> extends AbstractEventHandler<T> {

	private final UserNotificationService notificationService;

	protected AbstractUserNotificationHandler(EventPayloadMapper mapper, UserNotificationService notificationService) {
		super(mapper);
		this.notificationService = notificationService;
	}

	@Override
	protected final void process(T payload, IntegrationEventEnvelope<?> event) {
		updateProjection(payload, event);
		notificationService.publishUserUpdated(userId(payload), event.schemaVersion());
	}

	protected abstract void updateProjection(T payload, IntegrationEventEnvelope<?> event);

	protected abstract String userId(T payload);
}