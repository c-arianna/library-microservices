package mentoring.acomi.bookservice.application.aggregates;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.book.model.ISBN;
import mentoring.acomi.bookservice.domain.events.book.BookEvent;
import mentoring.acomi.bookservice.domain.events.book.BookEventType;
import mentoring.acomi.bookservice.infrastructure.messaging.BookIntegrationConsumerEventVersions;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;

@Component
public class BookAggregateFactory {
	
	private final BookEventRepository bookEventRepository;
	private final OutboxRepository outboxRepository;
	
	public BookAggregateFactory(BookEventRepository bookEventRepository, OutboxRepository outboxRepository) {
		this.bookEventRepository = bookEventRepository;
		this.outboxRepository = outboxRepository;
	}
	
	public BookAggregate create(String isbn) {
		List<BookEvent> events = bookEventRepository.loadStream(isbn);
		Consumer<BookEvent> dispatcher = this::persistEvent;
		return new BookAggregate(ISBN.of(isbn), dispatcher, events);
	}
	
	private void persistEvent(BookEvent event) {
		bookEventRepository.appendToStream(event, getSchemaVersion(event.type()));
		OutboxEvent pendingOutbox = new OutboxEvent(event.eventId(), event.aggregateType(), OutboxStatus.PENDING, 0, null, 
				Instant.now(), null, Instant.now());
		outboxRepository.add(pendingOutbox);
	}
	
	private int getSchemaVersion(BookEventType eventType) {
		return switch(eventType) {
		
		case BookBorrowRejected -> {
			yield BookIntegrationConsumerEventVersions.BOOK_BORROW_REJECTED;
		}
		case BookBorrowed-> {
			yield BookIntegrationConsumerEventVersions.BOOK_BORROWED;
		}
		case BookCopiesAdded-> {
			yield BookIntegrationConsumerEventVersions.BOOK_COPIES_UPDATED;
		}
		case BookCopiesRemoved-> {
			yield BookIntegrationConsumerEventVersions.BOOK_COPIES_UPDATED;
		}
		case BookRegistered-> {
			yield BookIntegrationConsumerEventVersions.BOOK_REGISTERED;
		}
		case BookReleased-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RELEASED;
		}
		case BookReservationRejected-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RESERVATION_REJECTED;
		}
		case BookReserved-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RESERVED;
		}
		case BookReturned-> {
			yield BookIntegrationConsumerEventVersions.BOOK_RETURNED;
		}
		
		};
	}
	
	

}
