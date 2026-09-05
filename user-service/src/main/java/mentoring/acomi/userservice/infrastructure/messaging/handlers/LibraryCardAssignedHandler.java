package mentoring.acomi.userservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.userservice.application.projection.UserProjectionOperations;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.LibraryCardAssignedIntegrationPayload;

@HandlerMetadata(eventType = IntegrationEventTypes.LIBRARY_CARD_ASSIGNED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class LibraryCardAssignedHandler extends AbstractEventHandler<LibraryCardAssignedIntegrationPayload> {
	
	private final UserProjectionOperations projectionOperations;
	
	public LibraryCardAssignedHandler(@Qualifier("liveUserProjection") UserProjectionOperations projectionOperations, EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
	}
	
    @Override
	protected Class<LibraryCardAssignedIntegrationPayload> payloadType() {
		return LibraryCardAssignedIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(LibraryCardAssignedIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projectionOperations.assignCardNumber(payload.userId(), payload.cardNumber(), event.occurredAt());
		return Optional.empty();
	}
	
}
