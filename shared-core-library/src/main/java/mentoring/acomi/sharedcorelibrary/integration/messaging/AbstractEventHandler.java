package mentoring.acomi.sharedcorelibrary.integration.messaging;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;

public abstract class AbstractEventHandler<T> implements EventHandler {

	private final EventPayloadMapper mapper;

	protected AbstractEventHandler(EventPayloadMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public final void handleEvent(IntegrationEventEnvelope<?> event) {
		T payload = mapper.mapAndValidate(event.payload(), payloadType());
		process(payload, event);
	}

	protected abstract Class<T> payloadType();

	protected abstract void process(T payload, IntegrationEventEnvelope<?> event);
	
}