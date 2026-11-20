package mentoring.acomi.sharedcorelibrary.integration.messaging.notifications;

import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;

public abstract class AbstractNotificationHandler<T> implements NotificationHandler {

	private final EventPayloadMapper mapper;

	protected AbstractNotificationHandler(EventPayloadMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public final void handleEvent(NotificationEventEnvelope<?> event) {
		T payload = mapper.mapAndValidate(event.payload(), payloadType());
		process(payload, event);
	}

	protected abstract Class<T> payloadType();

	protected abstract void process(T payload, NotificationEventEnvelope<?> event);
		
}