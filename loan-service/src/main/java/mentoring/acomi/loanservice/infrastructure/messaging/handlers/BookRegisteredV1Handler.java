package mentoring.acomi.loanservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.BookRegisteredIntegrationPayload;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REGISTERED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
@Component
public class BookRegisteredV1Handler extends AbstractEventHandler<BookRegisteredIntegrationPayload>{

	private final ProjectionDispatcher dispatcher;
	
	public BookRegisteredV1Handler(@Qualifier("liveDispatcher") ProjectionDispatcher dispatcher, EventPayloadMapper mapper) {
		super(mapper);
		this.dispatcher = dispatcher;
	}
	
	@Override
	protected Class<BookRegisteredIntegrationPayload> payloadType() {
		return BookRegisteredIntegrationPayload.class;
	}
	
	@Override
	protected Optional<ProjectionUpdateNotification> process(BookRegisteredIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		dispatcher.dispatch(event, payload);
		return Optional.empty();
	}
}
