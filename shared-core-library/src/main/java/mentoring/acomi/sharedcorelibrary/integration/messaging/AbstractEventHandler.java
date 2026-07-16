package mentoring.acomi.sharedcorelibrary.integration.messaging;

import java.util.Optional;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;

public abstract class AbstractEventHandler<T> implements EventHandler {

	private final EventPayloadMapper mapper;

	protected AbstractEventHandler(EventPayloadMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public final Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> event) {
		T payload = mapper.mapAndValidate(event.payload(), payloadType());
		return process(payload, event);
	}

	protected abstract Class<T> payloadType();

	protected abstract Optional<ProjectionUpdateNotification> process(T payload, IntegrationEventEnvelope<?> event);
	
}