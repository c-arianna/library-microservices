package mentoring.acomi.bookservice.infrastructure.messaging.handlers;

import org.springframework.stereotype.Component;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.bookservice.application.projection.BookProjection;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookBorrowedV1Handler implements EventHandler {
	
	private final BookProjection projection;
	private final EventPayloadMapper mapper;

	public BookBorrowedV1Handler(BookProjection projection, EventPayloadMapper mapper) {
		this.projection = projection;
		this.mapper = mapper;
	}
	
	@Override
	public IntegrationEventTypes eventType() {
		return IntegrationEventTypes.BOOK_BORROWED;
	}

	@Override
	public boolean accepts(IntegrationEventEnvelope<?> event) {
		return event.eventType() == eventType() && event.schemaVersion() == 1;
	}

	@Override
	public void handleEvent(IntegrationEventEnvelope<?> event) {
		BookLoanIntegrationPayload payload = mapper.mapAndValidate(event.payload(), BookLoanIntegrationPayload.class);
		projection.borrow(payload, event.occurredAt());
	}

}
