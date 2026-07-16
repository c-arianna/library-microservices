package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import java.util.Optional;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.AbstractEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REGISTERED, supportedVersions = {1})
@Component
public class BookRegisteredV1Handler extends AbstractEventHandler<BookRegisteredIntegrationPayload> {

	private final BookProjection projection;

	public BookRegisteredV1Handler(BookProjection projection, EventPayloadMapper mapper) {
		super(mapper);
		this.projection = projection;
	}

	@Override
	protected Class<BookRegisteredIntegrationPayload> payloadType() {
		return BookRegisteredIntegrationPayload.class;
	}

	@Override
	protected Optional<ProjectionUpdateNotification> process(BookRegisteredIntegrationPayload payload, IntegrationEventEnvelope<?> event) {
		projection.addBook(payload, event.occurredAt());
		return Optional.of(new ProjectionUpdateNotification(payload.isbn(), event.schemaVersion()));
	}

}
