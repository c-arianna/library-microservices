package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.LibraryCardAssignedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.LIBRARY_CARD_ASSIGNED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class LibraryCardAssignedHandler extends AbstractEventHandler<LibraryCardAssignedIntegrationPayload> {

	private final ProjectionDispatcher dispatcher;

	public LibraryCardAssignedHandler(@Qualifier("liveDispatcher") ProjectionDispatcher dispatcher, EventPayloadMapper mapper) {
		super(mapper);
		this.dispatcher = dispatcher;
	}
	
	@Override
	protected Class<LibraryCardAssignedIntegrationPayload> payloadType() {
		return LibraryCardAssignedIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(LibraryCardAssignedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		dispatcher.dispatch(event, payload);
		return Optional.empty();
	}

}