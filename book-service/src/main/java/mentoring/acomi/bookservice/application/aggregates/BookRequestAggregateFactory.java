package mentoring.acomi.bookservice.application.aggregates;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import mentoring.acomi.bookservice.application.repositories.BookRequestEventRepository;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEventType;
import mentoring.acomi.bookservice.infrastructure.messaging.BookIntegrationConsumerEventVersions;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;

public class BookRequestAggregateFactory {
	private final BookRequestEventRepository bookRequestEventRepository;
	private final OutboxRepository outboxRepository;

	public BookRequestAggregateFactory(BookRequestEventRepository bookRequestEventRepository, OutboxRepository outboxRepository) {
		this.bookRequestEventRepository = bookRequestEventRepository;
		this.outboxRepository = outboxRepository;
	}

	public BookRequestAggregate create(String requestId) {
		List<BookRequestEvent> events = bookRequestEventRepository.loadStream(requestId);
		Consumer<BookRequestEvent> dispatcher = this::persistEvent;
		return new BookRequestAggregate(requestId, dispatcher, events);
	}

	private void persistEvent(BookRequestEvent event) {
		bookRequestEventRepository.appendToStream(event, getSchemaVersion(event.type()));
		OutboxEvent pendingOutbox = new OutboxEvent(event.eventId(), event.aggregateType(), OutboxStatus.PENDING, 0,
				null, Instant.now(), null, Instant.now());
		outboxRepository.add(pendingOutbox);
	}

	private int getSchemaVersion(BookRequestEventType eventType) {
		return switch (eventType) {

			case BookRequestAdded -> {
				yield BookIntegrationConsumerEventVersions.BOOK_REQUEST_ADDED;
			}
			case BookRequestApproved -> {
				yield BookIntegrationConsumerEventVersions.BOOK_REQUEST_APPROVED;
			}
			case BookRequestRejected -> {
				yield BookIntegrationConsumerEventVersions.BOOK_REQUEST_REJECTED;
			}
			case BookRequestVoted -> {
				yield BookIntegrationConsumerEventVersions.BOOK_REQUEST_VOTED;
			}

		};
	}
}
