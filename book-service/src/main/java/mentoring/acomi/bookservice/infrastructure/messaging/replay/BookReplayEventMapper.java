package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.domain.events.payload.BookBorrowRejectedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookCopiesAddedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookCopiesRemovedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookRegisteredPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookReservationRejectedPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookReplayEventMapper {

	private final ObjectMapper mapper;

	public BookReplayEventMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	public IntegrationEventTypes toIntegrationEventType(BookEventType eventType) {
		
		return switch (eventType) {
		case BookBorrowRejected -> {
			yield IntegrationEventTypes.BOOK_BORROW_REJECTED;
		}
		case BookBorrowed -> {
			yield IntegrationEventTypes.BOOK_BORROWED;
		}
		case BookCopiesAdded, BookCopiesRemoved -> {
			yield IntegrationEventTypes.BOOK_COPIES_UPDATED;
		}
		case BookRegistered -> {
			yield IntegrationEventTypes.BOOK_REGISTERED;
		}
		case BookReleased -> {
			yield IntegrationEventTypes.BOOK_RELEASED;
		}
		case BookReservationRejected -> {
			yield IntegrationEventTypes.BOOK_RESERVATION_REJECTED;
		}
		case BookReserved -> {
			yield IntegrationEventTypes.BOOK_RESERVED;
		}
		case BookReturned -> {
			yield IntegrationEventTypes.BOOK_RETURNED;
		}
		};
	}
	
	public Object toIntegrationPayload(BookEventType eventType, JsonNode eventPayload) {
		return switch (eventType) {
		case BookBorrowRejected -> {
			BookBorrowRejectedPayload payload =  mapper.convertValue(eventPayload, BookBorrowRejectedPayload.class);
			yield new BookBorrowRejectedIntegrationPayload(payload.isbn(), payload.loanId(), payload.userId(), payload.reason().toString());
		}
		case BookBorrowed, BookReleased, BookReserved, BookReturned -> {
			BookLoanPayload payload = mapper.convertValue(eventPayload, BookLoanPayload.class);
			yield new BookLoanIntegrationPayload(payload.isbn(), payload.loanId(), payload.userId());
		}
		case BookCopiesAdded -> {
			BookCopiesAddedPayload payload = mapper.convertValue(eventPayload, BookCopiesAddedPayload.class);
			yield new BookCopiesUpdatedIntegrationPayload(payload.isbn(), payload.quantity());
		}
		case BookCopiesRemoved -> {
			BookCopiesRemovedPayload payload = mapper.convertValue(eventPayload, BookCopiesRemovedPayload.class);
			yield new BookCopiesUpdatedIntegrationPayload(payload.isbn(), -payload.quantity());
		}
		case BookRegistered -> {
			BookRegisteredPayload payload = mapper.convertValue(eventPayload, BookRegisteredPayload.class);
			yield new BookRegisteredIntegrationPayload(payload.isbn(), payload.author(), payload.title(), payload.description());
		}
		case BookReservationRejected -> {
			BookReservationRejectedPayload payload = mapper.convertValue(eventPayload, BookReservationRejectedPayload.class);
			yield new BookReservationRejectedIntegrationPayload(payload.isbn(), payload.loanId(), payload.userId(), payload.reason().toString());
		}
			
		};
		
	}
	
}
