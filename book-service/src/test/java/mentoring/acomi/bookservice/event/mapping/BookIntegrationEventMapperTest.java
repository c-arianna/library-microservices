package mentoring.acomi.bookservice.event.mapping;

import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import mentoring.acomi.bookservice.domain.events.BookBorrowRejectReason;
import mentoring.acomi.bookservice.domain.events.BookBorrowRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookBorrowedEvent;
import mentoring.acomi.bookservice.domain.events.BookCopiesAddedEvent;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.domain.events.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.BookReleasedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservationRejectReason;
import mentoring.acomi.bookservice.domain.events.BookReservationRejectedEvent;
import mentoring.acomi.bookservice.domain.events.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.BookReturnedEvent;
import mentoring.acomi.bookservice.domain.events.payload.BookBorrowRejectedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookCopiesAddedPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookRegisteredPayload;
import mentoring.acomi.bookservice.domain.events.payload.BookReservationRejectedPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.BookIntegrationEventMapper;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookBorrowRejectedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookCopiesUpdatedIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookLoanIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookRegisteredIntegrationPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.payload.producer.BookReservationRejectedIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;

public class BookIntegrationEventMapperTest {

	private static final String BOOK_BORROW_REJECTED_EVENT_NAME = IntegrationEventTypes.BOOK_BORROW_REJECTED.eventName;
	private static final String BOOK_RESERVATION_REJECTED_EVENT_NAME = IntegrationEventTypes.BOOK_RESERVATION_REJECTED.eventName;
	private static final String BOOK_RETURNED_EVENT_NAME = IntegrationEventTypes.BOOK_RETURNED.eventName;
	private static final String BOOK_RELEASED_EVENT_NAME = IntegrationEventTypes.BOOK_RELEASED.eventName;
	private static final String BOOK_RESERVED_EVENT_NAME = IntegrationEventTypes.BOOK_RESERVED.eventName;
	private static final String BOOK_COPIES_UPDATED_EVENT_NAME = IntegrationEventTypes.BOOK_COPIES_UPDATED.eventName;
	private static final String BOOK_BORROWED_EVENT_NAME = IntegrationEventTypes.BOOK_BORROWED.eventName;
	private static final String BOOK_REGISTERED_EVENT_NAME = IntegrationEventTypes.BOOK_REGISTERED.eventName;
	
	private static final String PRODUCER = "book-service";
	private final BookIntegrationEventMapper mapper = new BookIntegrationEventMapper();

	@ParameterizedTest(name = "[{index}] set correct metadata -> {0}")
	@MethodSource("eventCases")
	void shouldSetCorrectMetadata(String name, BookEvent domainEvent, IntegrationEventTypes eventType) {

		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);

		Assertions.assertEquals(eventType, event.eventType());
		Assertions.assertEquals(PRODUCER, event.producer());
		Assertions.assertEquals(1, event.schemaVersion());
		Assertions.assertNotNull(event.eventId());
	}

	static Stream<Arguments> eventCases() {
        return Stream.of(
                Arguments.of(BOOK_REGISTERED_EVENT_NAME, getBookRegisteredEvent(), IntegrationEventTypes.BOOK_REGISTERED),
                Arguments.of(BOOK_BORROWED_EVENT_NAME, getBookBorrowedEvent(), IntegrationEventTypes.BOOK_BORROWED),
                Arguments.of(BOOK_COPIES_UPDATED_EVENT_NAME, getBookCopiesAddedEvent(), IntegrationEventTypes.BOOK_COPIES_UPDATED),
                Arguments.of(BOOK_RESERVED_EVENT_NAME, getBookReservedEvent(), IntegrationEventTypes.BOOK_RESERVED),
                Arguments.of(BOOK_RELEASED_EVENT_NAME, getBookReleasedEvent(), IntegrationEventTypes.BOOK_RELEASED),
                Arguments.of(BOOK_RETURNED_EVENT_NAME, getBookReturnedEvent(), IntegrationEventTypes.BOOK_RETURNED),
                Arguments.of(BOOK_RESERVATION_REJECTED_EVENT_NAME, getBookReservationRejectedEvent(), IntegrationEventTypes.BOOK_RESERVATION_REJECTED),
                Arguments.of(BOOK_BORROW_REJECTED_EVENT_NAME, getBookBorrowRejectedEvent(), IntegrationEventTypes.BOOK_BORROW_REJECTED)
               
        );
    }
	
	@Test
	void shouldMapBookRegisteredIntegrationPayloadCorrectly() {
	
		BookRegisteredEvent domainEvent = getBookRegisteredEvent();
	
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
	
		BookRegisteredIntegrationPayload integrationPayload = (BookRegisteredIntegrationPayload) event.payload();
		BookRegisteredPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.isbn(), integrationPayload.isbn());
		Assertions.assertEquals(payload.author(), integrationPayload.author());
		Assertions.assertEquals(payload.title(), integrationPayload.title());
		Assertions.assertEquals(payload.description(), integrationPayload.description());
	 
	}
	
	@Test
	void shouldMapBookBorrowRejectedIntegrationPayloadCorrectly() {
	
		BookBorrowRejectedEvent domainEvent = getBookBorrowRejectedEvent();
	
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);

		BookBorrowRejectedIntegrationPayload integrationPayload = (BookBorrowRejectedIntegrationPayload) event.payload();
		BookBorrowRejectedPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.isbn(), integrationPayload.isbn());
		Assertions.assertEquals(payload.loanId(), integrationPayload.loanId());
		Assertions.assertEquals(payload.userId(), integrationPayload.userId());
		Assertions.assertEquals(payload.reason().toString(), integrationPayload.reason());
	 
	}
	
	@Test
	void shouldMapBookCopiesUpdatedIntegrationPayloadCorrectly() {
	
		BookCopiesAddedEvent domainEvent = getBookCopiesAddedEvent();
	
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
	
		BookCopiesUpdatedIntegrationPayload integrationPayload = (BookCopiesUpdatedIntegrationPayload) event.payload();
		BookCopiesAddedPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.isbn(), integrationPayload.isbn());
		Assertions.assertEquals(payload.quantity(), integrationPayload.quantity());
		
	}
	
	@Test
	void shouldMapBookLoanIntegrationPayloadCorrectly() {
	
		BookReservedEvent domainEvent = getBookReservedEvent();
	
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
	
		BookLoanIntegrationPayload integrationPayload = (BookLoanIntegrationPayload) event.payload();
		BookLoanPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.isbn(), integrationPayload.isbn());
		Assertions.assertEquals(payload.userId(), integrationPayload.userId());
		Assertions.assertEquals(payload.loanId(), integrationPayload.loanId());
			 
	}
	
	@Test
	void shouldMapBookReservationRejectedIntegrationPayloadCorrectly() {
	
		BookReservationRejectedEvent domainEvent = getBookReservationRejectedEvent();
	
		IntegrationEventEnvelope<?> event = mapper.map(domainEvent);
	
	
		BookReservationRejectedIntegrationPayload integrationPayload = (BookReservationRejectedIntegrationPayload) event.payload();
		BookReservationRejectedPayload payload = domainEvent.payload();
		
		Assertions.assertEquals(payload.isbn(), integrationPayload.isbn());
		Assertions.assertEquals(payload.loanId(), integrationPayload.loanId());
		Assertions.assertEquals(payload.userId(), integrationPayload.userId());
		Assertions.assertEquals(payload.reason().toString(), integrationPayload.reason());
	 
	}

	private static BookRegisteredEvent getBookRegisteredEvent() {
		BookRegisteredPayload payload = new BookRegisteredPayload("9788828606819", "Italo Calvino", "Il visconte dimezzato", "trilogia");
		return new BookRegisteredEvent("9788828606819", "event-1", 0, payload, Instant.now());
	}
	
	private static BookBorrowRejectedEvent getBookBorrowRejectedEvent() {
		BookBorrowRejectedPayload payload = new BookBorrowRejectedPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", 
    			"203a9860-cdd8-4c33-a67f-45a6cf6e2799", BookBorrowRejectReason.BOOK_NOT_REGISTERED);
		
		return new BookBorrowRejectedEvent("9788828606819", "event-1", 0, payload, Instant.now());
	}
	
	private static BookCopiesAddedEvent getBookCopiesAddedEvent() {
		BookCopiesAddedPayload payload = new BookCopiesAddedPayload("9788828606819", 3);
		return new BookCopiesAddedEvent("9788828606819", "event-1", 0, payload, Instant.now());
	}
	
	private static BookReservedEvent getBookReservedEvent() {
		BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
		return new BookReservedEvent("9788828606819", "event-1", 0, payload, Instant.now());
	}
	
	private static BookReservationRejectedEvent getBookReservationRejectedEvent() {
		BookReservationRejectedPayload payload = new BookReservationRejectedPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", 
    			"203a9860-cdd8-4c33-a67f-45a6cf6e2799", BookReservationRejectReason.BOOK_NOT_REGISTERED);
		return new BookReservationRejectedEvent("9788828606819", "event-1", 0, payload, Instant.now());
	}
	
	private static BookReturnedEvent getBookReturnedEvent() {
		BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	return new BookReturnedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
	}

	private static BookReleasedEvent getBookReleasedEvent() {
		BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	return new BookReleasedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
	}

	private static BookBorrowedEvent getBookBorrowedEvent() {
		BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
        return new BookBorrowedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
	}
	
}