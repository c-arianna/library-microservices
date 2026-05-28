package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.BookBorrowRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookBorrowedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesAddedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesRemovedEvent;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.domain.events.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.BookReleasedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservationRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.BookReturnedEvent;
import mentoring.acomi.bookservice.domain.events.payload.BookBorrowRejectedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookCopiesAddedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookCopiesRemovedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookRegisteredPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookReservationRejectedPayload;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookEventJpaMapper {

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
			yield new BookRegisteredEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
		}

		case BookCopiesAdded -> {
			BookCopiesAddedPayload payload = objectMapper.treeToValue(event.getPayload(), BookCopiesAddedPayload.class);
			yield new BookCopiesAddedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
		}

		case BookCopiesRemoved -> {
			BookCopiesRemovedPayload payload = objectMapper.treeToValue(event.getPayload(),
					BookCopiesRemovedPayload.class);
			yield new BookCopiesRemovedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
		}

		case BookReserved -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(),
					BookLoanPayload.class);
			yield new BookReservedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
			
		}
		
		case BookReservationRejected -> {
			BookReservationRejectedPayload payload = objectMapper.treeToValue(event.getPayload(),
					BookReservationRejectedPayload.class);
			yield new BookReservationRejectedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
			
		}
		
		case BookBorrowed -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(), BookLoanPayload.class);
			yield new BookBorrowedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
			
		}
		
		case BookBorrowRejected -> {
			BookBorrowRejectedPayload payload = objectMapper.treeToValue(event.getPayload(), BookBorrowRejectedPayload.class);
			yield new BookBorrowRejectedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
			
		}
		
		case BookReleased -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(), BookLoanPayload.class);
			yield new BookReleasedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
			
		}
		
		case BookReturned -> {
			BookLoanPayload payload = objectMapper.treeToValue(event.getPayload(), BookLoanPayload.class);
			yield new BookReturnedEvent(event.getAggregateId(), event.getEventId(), payload, event.getOccurredAt());
			
		}
		
				
		};

	}

	public BookEventEntity toEntity(BookEvent event) {
		return new BookEventEntity(event.aggregateId(), event.type().name(), event.eventId(),
				toJsonNode(event.payload()), event.occurredAt());

	}

	private JsonNode toJsonNode(Object payload) {
		return objectMapper.valueToTree(payload);
	}

}
