package mentoring.acomi.bookservice.infrastructure.messaging;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.BookBorrowRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookBorrowedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesAddedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesRemovedEvent;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.BookReleasedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservationRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.BookReturnedEvent;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;

@Component
public class BookIntegrationEventMapper {

	private static final String PRODUCER = "book-service";

	public IntegrationEventEnvelope<?> map(BookEvent event) {
		return switch (event) {

		case BookRegisteredEvent e -> {
			yield getBookRegisteredIntegrationEvent(e);
		}

		case BookCopiesAddedEvent e -> {
			yield getBookCopiesAddedIntegrationEvent(e);
		}

		case BookCopiesRemovedEvent e -> {
			yield getBookCopiesRemoveIntegrationEvent(e);
		}

		case BookReservedEvent e -> {
			yield getBookReservedIntegrationEvent(e);
		}

		case BookBorrowedEvent e -> {
			yield getBookBorrowedIntegrationEvent(e);
		}

		case BookReleasedEvent e -> {
			yield getBookReleasedIntegrationEvent(e);
		}

		case BookReturnedEvent e -> {
			yield getBookReturnedIntegrationEvent(e);
		}

		case BookReservationRejectedEvent e -> {
			yield getBookReservationRejectedIntegrationEvent(e);
		}

		case BookBorrowRejectedEvent e -> {
			yield getBookBorrowRejectedIntegrationevent(e);
		}
		};
	}

	private IntegrationEventEnvelope<?> getBookBorrowRejectedIntegrationevent(BookBorrowRejectedEvent e) {

		BookBorrowRejectedIntegrationPayload payload = new BookBorrowRejectedIntegrationPayload(e.payload().isbn(),
				e.payload().loanId(), e.payload().userId(), e.payload().reason().toString());

		return envelope(e, IntegrationEventTypes.BOOK_BORROW_REJECTED,
				BookIntegrationPublisherEventVersions.BOOK_BORROW_REJECTED, payload);
	}

	private IntegrationEventEnvelope<?> getBookReservationRejectedIntegrationEvent(BookReservationRejectedEvent e) {

		BookReservationRejectedIntegrationPayload payload = new BookReservationRejectedIntegrationPayload(
				e.payload().isbn(), e.payload().loanId(), e.payload().userId(), e.payload().reason().toString());

		return envelope(e, IntegrationEventTypes.BOOK_RESERVATION_REJECTED,
				BookIntegrationPublisherEventVersions.BOOK_RESERVATION_REJECTED, payload);
	}

	private IntegrationEventEnvelope<?> getBookReturnedIntegrationEvent(BookReturnedEvent e) {

		BookLoanIntegrationPayload payload = new BookLoanIntegrationPayload(e.payload().isbn(), e.payload().loanId(),
				e.payload().userId());

		return envelope(e, IntegrationEventTypes.BOOK_RETURNED, BookIntegrationPublisherEventVersions.BOOK_RETURNED, payload);
	}

	private IntegrationEventEnvelope<?> getBookReleasedIntegrationEvent(BookReleasedEvent e) {

		BookLoanIntegrationPayload payload = new BookLoanIntegrationPayload(e.payload().isbn(), e.payload().loanId(),
				e.payload().userId());

		return envelope(e, IntegrationEventTypes.BOOK_RELEASED, BookIntegrationPublisherEventVersions.BOOK_RELEASED, payload);
	}

	private IntegrationEventEnvelope<?> getBookBorrowedIntegrationEvent(BookBorrowedEvent e) {

		BookLoanIntegrationPayload payload = new BookLoanIntegrationPayload(e.payload().isbn(), e.payload().loanId(),
				e.payload().userId());

		return envelope(e, IntegrationEventTypes.BOOK_BORROWED, BookIntegrationPublisherEventVersions.BOOK_BORROWED, payload);
	}

	private IntegrationEventEnvelope<?> getBookReservedIntegrationEvent(BookReservedEvent e) {

		BookLoanIntegrationPayload payload = new BookLoanIntegrationPayload(e.payload().isbn(), e.payload().loanId(),
				e.payload().userId());

		return envelope(e, IntegrationEventTypes.BOOK_RESERVED, BookIntegrationPublisherEventVersions.BOOK_RESERVED, payload);
	}

	private IntegrationEventEnvelope<?> getBookCopiesRemoveIntegrationEvent(BookCopiesRemovedEvent e) {

		BookCopiesUpdatedIntegrationPayload payload = new BookCopiesUpdatedIntegrationPayload(e.payload().isbn(),
				-e.payload().quantity());

		return envelope(e, IntegrationEventTypes.BOOK_COPIES_UPDATED, BookIntegrationPublisherEventVersions.BOOK_COPIES_UPDATED,
				payload);
	}

	private IntegrationEventEnvelope<?> getBookCopiesAddedIntegrationEvent(BookCopiesAddedEvent e) {

		BookCopiesUpdatedIntegrationPayload payload = new BookCopiesUpdatedIntegrationPayload(e.payload().isbn(),
				e.payload().quantity());

		return envelope(e, IntegrationEventTypes.BOOK_COPIES_UPDATED, BookIntegrationPublisherEventVersions.BOOK_COPIES_UPDATED,
				payload);
	}

	private IntegrationEventEnvelope<?> getBookRegisteredIntegrationEvent(BookRegisteredEvent e) {

		BookRegisteredIntegrationPayload payload = new BookRegisteredIntegrationPayload(e.payload().isbn(),
				e.payload().author(), e.payload().title(), e.payload().description());

		return envelope(e, IntegrationEventTypes.BOOK_REGISTERED, BookIntegrationPublisherEventVersions.BOOK_REGISTERED,
				payload);
	}

	private <T> IntegrationEventEnvelope<T> envelope(BookEvent e, IntegrationEventTypes type, int version, T payload) {
		return new IntegrationEventEnvelope<>(e.eventId(), type, PRODUCER, e.aggregateId(), AggregateType.BOOK.name(), e.eventVersion(), e.occurredAt(), version, payload);
	}

}
