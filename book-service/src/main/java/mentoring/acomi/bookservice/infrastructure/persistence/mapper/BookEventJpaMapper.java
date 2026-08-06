package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.book.BookBorrowRejectedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookBorrowedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookCopiesAddedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookCopiesRemovedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookEvent;
import mentoring.acomi.bookservice.domain.events.book.BookEventType;
import mentoring.acomi.bookservice.domain.events.book.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReleasedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReservationRejectedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReturnedEvent;
import mentoring.acomi.bookservice.domain.events.book.payload.BookBorrowRejectedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookCopiesAddedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookCopiesRemovedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookRegisteredPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookReservationRejectedPayload;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookEventJpaMapper implements EventMapper<BookEvent, BookEventEntity>{

	private final ObjectMapper objectMapper;

	public BookEventJpaMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public BookEvent toDomain(BookEventEntity event) {

		BookEventType eventType = BookEventType.valueOf(event.getEventType());

		return getEvent(eventType, event);
	}

	private BookEvent getEvent(BookEventType eventType, BookEventEntity event) {

		return switch (eventType) {

		case BookRegistered -> {
			BookRegisteredPayload payload = objectMapper.treeToValue(event.getPayload(), BookRegisteredPayload.class);
			yield new BookRegisteredEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}

		case BookCopiesAdded -> {
			BookCopiesAddedPayload payload = objectMapper.treeToValue(event.getPayload(), BookCopiesAddedPayload.class);
			yield new BookCopiesAddedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}

		case BookCopiesRemoved -> {
			BookCopiesRemovedPayload payload = objectMapper.treeToValue(event.getPayload(),
					BookCopiesRemovedPayload.class);
			yield new BookCopiesRemovedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}

		case BookReserved -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(),
					BookLoanPayload.class);
			yield new BookReservedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
			
		}
		
		case BookReservationRejected -> {
			BookReservationRejectedPayload payload = objectMapper.treeToValue(event.getPayload(),
					BookReservationRejectedPayload.class);
			yield new BookReservationRejectedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
			
		}
		
		case BookBorrowed -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(), BookLoanPayload.class);
			yield new BookBorrowedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
			
		}
		
		case BookBorrowRejected -> {
			BookBorrowRejectedPayload payload = objectMapper.treeToValue(event.getPayload(), BookBorrowRejectedPayload.class);
			yield new BookBorrowRejectedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
			
		}
		
		case BookReleased -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(), BookLoanPayload.class);
			yield new BookReleasedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
			
		}
		
		case BookReturned -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(), BookLoanPayload.class);
			yield new BookReturnedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
			
		}
		
		default -> throw new IllegalArgumentException(String.format("Unexpected value: %s", eventType.name()));
		
				
		};

	}

	public BookEventEntity toEntity(BookEvent event) {
		return new BookEventEntity(event.aggregateId(), event.aggregateType(), event.type().name(), event.eventId(), event.eventVersion(), null, toJsonNode(event.payload()), event.occurredAt());
	}

	private JsonNode toJsonNode(Object payload) {
		return objectMapper.valueToTree(payload);
	}

}
