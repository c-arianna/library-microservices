package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestAddedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestApprovedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestPriceUpdatedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestRejectedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestVotedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestAddedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestApprovedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestPriceUpdatedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestRejectedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestVotedPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestAddedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestApprovedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestPriceUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestRejectedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestVotedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookRequestIntegrationEventMapper {
	
	private static final String PRODUCER = "book-service";

	public IntegrationEventEnvelope<?> map(BookRequestEvent event) {
		return switch (event) {

		case BookRequestAddedEvent e -> {
			yield getBookRequestAddedEvent(e);
		}
		
		case BookRequestApprovedEvent e -> {
			yield getBookRequestApprovedEvent(e);
		}
		
		case BookRequestVotedEvent e -> {
			yield getBookRequestVotedEvent(e);
		}
		
		case BookRequestRejectedEvent e -> {
			yield getBookRequestRejectedEvent(e);
		}

		case BookRequestPriceUpdatedEvent e -> {
			yield getBookRequestPriceUpdatedEvent(e);
		}
		
		};
	}

	private IntegrationEventEnvelope<?> getBookRequestAddedEvent(BookRequestAddedEvent e) {

		BookRequestAddedPayload payload = e.payload();
		BookRequestAddedIntegrationPayload integrationPayload = new BookRequestAddedIntegrationPayload(payload.requestId(), 
				payload.author(), payload.title(), payload.requesterUserId(), payload.isbn(), payload.notes());

		return envelope(e, IntegrationEventTypes.BOOK_REQUEST_ADDED, BookIntegrationPublisherEventVersions.BOOK_REQUEST_ADDED, 
				integrationPayload);
	}
	
	private IntegrationEventEnvelope<?> getBookRequestApprovedEvent(BookRequestApprovedEvent e) {

		BookRequestApprovedPayload payload = e.payload();
		BookRequestApprovedIntegrationPayload integrationPayload = new BookRequestApprovedIntegrationPayload(payload.requestId());

		return envelope(e, IntegrationEventTypes.BOOK_REQUEST_APPROVED, BookIntegrationPublisherEventVersions.BOOK_REQUEST_APPROVED, 
				integrationPayload);
	}
	
	private IntegrationEventEnvelope<?> getBookRequestVotedEvent(BookRequestVotedEvent e) {

		BookRequestVotedPayload payload = e.payload();
		BookRequestVotedIntegrationPayload integrationPayload = new BookRequestVotedIntegrationPayload(payload.requestId(), payload.userId());

		return envelope(e, IntegrationEventTypes.BOOK_REQUEST_VOTED, BookIntegrationPublisherEventVersions.BOOK_REQUEST_VOTED, 
				integrationPayload);
	}
	
	private IntegrationEventEnvelope<?> getBookRequestRejectedEvent(BookRequestRejectedEvent e) {

		BookRequestRejectedPayload payload = e.payload();
		BookRequestRejectedIntegrationPayload integrationPayload = new BookRequestRejectedIntegrationPayload(payload.requestId(), 
				payload.reason());

		return envelope(e, IntegrationEventTypes.BOOK_REQUEST_REJECTED, BookIntegrationPublisherEventVersions.BOOK_REQUEST_REJECTED, 
				integrationPayload);
	}
	
	private IntegrationEventEnvelope<?> getBookRequestPriceUpdatedEvent(BookRequestPriceUpdatedEvent e) {

		BookRequestPriceUpdatedPayload payload = e.payload();
		BookRequestPriceUpdatedIntegrationPayload integrationPayload = new BookRequestPriceUpdatedIntegrationPayload(payload.requestId(), 
				payload.estimatedPrice());

		return envelope(e, IntegrationEventTypes.BOOK_REQUEST_PRICE_UPDATED, 
				BookIntegrationPublisherEventVersions.BOOK_REQUEST_PRICE_UPDATED, integrationPayload);
	}
	
	private <T> IntegrationEventEnvelope<T> envelope(BookRequestEvent e, IntegrationEventTypes type, int version, T payload) {
		return new IntegrationEventEnvelope<>(e.eventId(), type, PRODUCER, e.aggregateId(), AggregateType.BOOK_REQUEST.name(), 
				e.eventVersion(), e.occurredAt(), version, payload);
	}

}
