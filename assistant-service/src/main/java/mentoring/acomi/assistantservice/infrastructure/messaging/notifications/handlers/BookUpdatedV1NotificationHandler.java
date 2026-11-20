package mentoring.acomi.assistantservice.infrastructure.messaging.notifications.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.assistantservice.application.projection.BookEmbeddingProjection;
import mentoring.acomi.assistantservice.application.projection.ProjectionOperations;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.BookUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.AbstractNotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;

@NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_UPDATED, supportedVersions = {1})
@Component
public class BookUpdatedV1NotificationHandler extends AbstractNotificationHandler<BookUpdatedNotificationPayload> {

	private final ProjectionOperations<BookView> projectionOperations;
	private final BookEmbeddingProjection embeddingProjection;
	
	public BookUpdatedV1NotificationHandler(ProjectionOperations<BookView> projectionOperations, BookEmbeddingProjection embeddinProjection,
			EventPayloadMapper mapper) {
		super(mapper);
		this.projectionOperations = projectionOperations;
		this.embeddingProjection = embeddinProjection;
	}
	
	@Override
	protected Class<BookUpdatedNotificationPayload> payloadType() {
		return BookUpdatedNotificationPayload.class;
	}
	
	@Override
	public void process(BookUpdatedNotificationPayload payload, NotificationEventEnvelope<?> event) {
		BookView view = getBook(payload);
		projectionOperations.upsert(view, event.occurredAt());		
		embeddingProjection.index(view);
	}

	public BookView getBook(BookUpdatedNotificationPayload payload) {		
		return new BookView(payload.isbn(), payload.author(), payload.title(), payload.description(), payload.totalCopies(), 
				payload.borrowedCopies(), payload.reservedCopies());
	}

}