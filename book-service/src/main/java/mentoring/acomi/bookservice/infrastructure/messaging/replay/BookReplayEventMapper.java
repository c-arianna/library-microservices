package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.ProducerEventType;
import mentoring.acomi.bookservice.domain.events.book.payload.BookBorrowRejectedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookCopiesAddedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookCopiesRemovedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookRegisteredPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookReservationRejectedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestAddedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestApprovedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestRejectedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestVotedPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestAddedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestApprovedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestRejectedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRequestVotedIntegrationPayload;
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
	
	public IntegrationEventTypes toIntegrationEventType(ProducerEventType eventType) {
		
		return switch (eventType.eventName()) {
			
			case "BookBorrowRejected" -> {
				yield IntegrationEventTypes.BOOK_BORROW_REJECTED;
			}
			case "BookBorrowed" -> {
				yield IntegrationEventTypes.BOOK_BORROWED;
			}
			case "BookCopiesAdded", "BookCopiesRemoved" -> {
				yield IntegrationEventTypes.BOOK_COPIES_UPDATED;
			}
			case "BookRegistered" -> {
				yield IntegrationEventTypes.BOOK_REGISTERED;
			}
			case "BookReleased" -> {
				yield IntegrationEventTypes.BOOK_RELEASED;
			}
			case "BookReservationRejected" -> {
				yield IntegrationEventTypes.BOOK_RESERVATION_REJECTED;
			}
			case "BookReserved" -> {
				yield IntegrationEventTypes.BOOK_RESERVED;
			}
			case "BookReturned" -> {
				yield IntegrationEventTypes.BOOK_RETURNED;
			}
			
			case "BookRequestAdded" -> {
				yield IntegrationEventTypes.BOOK_REQUEST_ADDED;
			}
			
			case "BookRequestApproved" -> {
				yield IntegrationEventTypes.BOOK_REQUEST_APPROVED;
			}
			
			case "BookRequestRejected" -> {
				yield IntegrationEventTypes.BOOK_REQUEST_REJECTED;
			}
			
			case "BookRequestVoted" -> {
				yield IntegrationEventTypes.BOOK_REQUEST_VOTED;
			}
			
			default -> throw new IllegalArgumentException("Unexpected value: %s".formatted(eventType.eventName()));
		};
	}
		
	public Object toIntegrationPayload(ProducerEventType eventType, JsonNode eventPayload) {
		
		return switch (eventType.eventName()) {
			
			case "BookBorrowRejected" -> {
				BookBorrowRejectedPayload payload =  mapper.convertValue(eventPayload, BookBorrowRejectedPayload.class);
				yield new BookBorrowRejectedIntegrationPayload(payload.isbn(), payload.loanId(), payload.userId(), payload.reason().toString());
			}
			case "BookBorrowed", "BookReleased", "BookReserved", "BookReturned" -> {
				BookLoanPayload payload = mapper.convertValue(eventPayload, BookLoanPayload.class);
				yield new BookLoanIntegrationPayload(payload.isbn(), payload.loanId(), payload.userId());
			}
			case "BookCopiesAdded" -> {
				BookCopiesAddedPayload payload = mapper.convertValue(eventPayload, BookCopiesAddedPayload.class);
				yield new BookCopiesUpdatedIntegrationPayload(payload.isbn(), payload.quantity());
			}
			case "BookCopiesRemoved" -> {
				BookCopiesRemovedPayload payload = mapper.convertValue(eventPayload, BookCopiesRemovedPayload.class);
				yield new BookCopiesUpdatedIntegrationPayload(payload.isbn(), -payload.quantity());
			}
			case "BookRegistered" -> {
				BookRegisteredPayload payload = mapper.convertValue(eventPayload, BookRegisteredPayload.class);
				yield new BookRegisteredIntegrationPayload(payload.isbn(), payload.author(), payload.title(), payload.description());
			}
			case "BookReservationRejected" -> {
				BookReservationRejectedPayload payload = mapper.convertValue(eventPayload, BookReservationRejectedPayload.class);
				yield new BookReservationRejectedIntegrationPayload(payload.isbn(), payload.loanId(), payload.userId(), payload.reason().toString());
			}
		
			case "BookRequestAdded" -> {
				BookRequestAddedPayload payload =  mapper.convertValue(eventPayload, BookRequestAddedPayload.class);
				yield new BookRequestAddedIntegrationPayload(payload.requestId(), payload.author(), payload.title(), payload.requesterUserId(),
						payload.isbn(), payload.notes());
			}
			
			case "BookRequestApproved" -> {
				BookRequestApprovedPayload payload =  mapper.convertValue(eventPayload, BookRequestApprovedPayload.class);
				yield new BookRequestApprovedIntegrationPayload(payload.requestId());
			}
			
			case "BookRequestRejected" -> {
				BookRequestRejectedPayload payload =  mapper.convertValue(eventPayload, BookRequestRejectedPayload.class);
				yield new BookRequestRejectedIntegrationPayload(payload.requestId(), payload.reason());
			}
			
			case "BookRequestVoted" -> {
				BookRequestVotedPayload payload =  mapper.convertValue(eventPayload, BookRequestVotedPayload.class);
				yield new BookRequestVotedIntegrationPayload(payload.requestId(), payload.userId());
			}
			default -> throw new IllegalArgumentException("Unexpected value: %s".formatted(eventType.eventName()));
			
		};
		
	}
	
}
