package mentoring.acomi.bookservice.event.contract.producer;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;

import mentoring.acomi.bookservice.domain.events.book.BookBorrowRejectReason;
import mentoring.acomi.bookservice.domain.events.book.BookBorrowRejectedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookBorrowedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookCopiesAddedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookEvent;
import mentoring.acomi.bookservice.domain.events.book.BookRegisteredEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReleasedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReservationRejectReason;
import mentoring.acomi.bookservice.domain.events.book.BookReservationRejectedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReservedEvent;
import mentoring.acomi.bookservice.domain.events.book.BookReturnedEvent;
import mentoring.acomi.bookservice.domain.events.book.payload.BookBorrowRejectedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookCopiesAddedPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookLoanPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookRegisteredPayload;
import mentoring.acomi.bookservice.domain.events.book.payload.BookReservationRejectedPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.BookIntegrationEventMapper;
import mentoring.acomi.contracts.support.JsonSchemaSupport;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class BookEventProducerContractTest extends JsonSchemaSupport {

	private static final String BOOK_BORROW_REJECTED_EVENT_NAME = IntegrationEventTypes.BOOK_BORROW_REJECTED.eventName;
	private static final String BOOK_RESERVATION_REJECTED_EVENT_NAME = IntegrationEventTypes.BOOK_RESERVATION_REJECTED.eventName;
	private static final String BOOK_RETURNED_EVENT_NAME = IntegrationEventTypes.BOOK_RETURNED.eventName;
	private static final String BOOK_RELEASED_EVENT_NAME = IntegrationEventTypes.BOOK_RELEASED.eventName;
	private static final String BOOK_RESERVED_EVENT_NAME = IntegrationEventTypes.BOOK_RESERVED.eventName;
	private static final String BOOK_COPIES_UPDATED_EVENT_NAME = IntegrationEventTypes.BOOK_COPIES_UPDATED.eventName;
	private static final String BOOK_BORROWED_EVENT_NAME = IntegrationEventTypes.BOOK_BORROWED.eventName;
	private static final String BOOK_REGISTERED_EVENT_NAME = IntegrationEventTypes.BOOK_REGISTERED.eventName;
	
	private static final String SCHEMA_PATH = "contracts/book/%s/v1/event.schema.json";
	private static final String SAMPLE_PATH = "contracts/book/%s/v1/sample.json";
	
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BookIntegrationEventMapper mapper = new BookIntegrationEventMapper();
    
    @ParameterizedTest(name = "[{index}] valid producer event -> {0}")
    @MethodSource("validProducerCases")
    void shouldProduceValidEvent(String name, BookContractCase testCase) {
        validateSchema(testCase.schemaPath(), testCase.domainEvent());
    }

    @ParameterizedTest(name = "[{index}] valid sample -> {0}")
    @MethodSource("sampleCases")
    void sampleShouldBeValid(String name, BookContractCase testCase) throws Exception {
        validateSample(testCase.samplePath(), testCase.schemaPath());
    }

    @ParameterizedTest(name = "[{index}] failed invalid json -> {0}")
    @MethodSource("invalidCases")
    void shouldFailIfMissingRequiredField(String name, BookContractCase testCase) throws Exception {
        validateInvalidJson(testCase.invalidJson(), testCase.schemaPath());
    }

    static Stream<Arguments> validProducerCases() {
        return Stream.of(
                Arguments.of(BOOK_REGISTERED_EVENT_NAME, bookRegisteredCase()),
                Arguments.of(BOOK_BORROWED_EVENT_NAME, bookBorrowedCase()),
                Arguments.of(BOOK_COPIES_UPDATED_EVENT_NAME, bookCopiesUpdateCase()),
                Arguments.of(BOOK_RESERVED_EVENT_NAME, bookReservedCase()),
                Arguments.of(BOOK_RELEASED_EVENT_NAME, bookReleasedCase()),
                Arguments.of(BOOK_RETURNED_EVENT_NAME, bookReturnedCase()),
                Arguments.of(BOOK_RESERVATION_REJECTED_EVENT_NAME, bookReservationRejectedCase()),
                Arguments.of(BOOK_BORROW_REJECTED_EVENT_NAME, bookBorrowRejectedCase())
               
        );
    }

    static Stream<Arguments> sampleCases() {
        return Stream.of(
                Arguments.of(BOOK_REGISTERED_EVENT_NAME, bookRegisteredCase()),
                Arguments.of(BOOK_BORROWED_EVENT_NAME, bookBorrowedCase()),
                Arguments.of(BOOK_COPIES_UPDATED_EVENT_NAME, bookCopiesUpdateCase()),
                Arguments.of(BOOK_RESERVED_EVENT_NAME, bookReservedCase()),
                Arguments.of(BOOK_RELEASED_EVENT_NAME, bookReleasedCase()),
                Arguments.of(BOOK_RETURNED_EVENT_NAME, bookReturnedCase()),
                Arguments.of(BOOK_RESERVATION_REJECTED_EVENT_NAME, bookReservationRejectedCase()),
                Arguments.of(BOOK_BORROW_REJECTED_EVENT_NAME, bookBorrowRejectedCase())
              
        );
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of(BOOK_REGISTERED_EVENT_NAME, bookRegisteredCase()),
                Arguments.of(BOOK_BORROWED_EVENT_NAME, bookBorrowedCase()),
                Arguments.of(BOOK_COPIES_UPDATED_EVENT_NAME, bookCopiesUpdateCase()),
                Arguments.of(BOOK_RESERVED_EVENT_NAME, bookReservedCase()),
                Arguments.of(BOOK_RELEASED_EVENT_NAME, bookReleasedCase()),
                Arguments.of(BOOK_RETURNED_EVENT_NAME, bookReturnedCase()),
                Arguments.of(BOOK_RESERVATION_REJECTED_EVENT_NAME, bookReservationRejectedCase()),
                Arguments.of(BOOK_BORROW_REJECTED_EVENT_NAME, bookBorrowRejectedCase())
        );
    }

    private static BookContractCase bookRegisteredCase() {
        
    	BookRegisteredPayload payload = new BookRegisteredPayload("9788828606819", "Italo Calvino", "Il visconte dimezzato", "Trilogia");

        BookRegisteredEvent event = new BookRegisteredEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c6", 0, payload, Instant.now());

        String invalidJson = """
            {
              "eventId": "id",
              "eventType": "BOOK_REGISTERED",
              "producer": "book-service",
              "aggregateId": "isbn",
              "occurredAt": "2026-06-09T10:00:00Z",
              "schemaVersion": 1,
              "payload": {
                "isbn": "123"
              }
            }
            """;

        return getBookContractCase(BOOK_REGISTERED_EVENT_NAME, event, invalidJson);
    }

    private static BookContractCase bookBorrowedCase() {
    	
        BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");

        BookBorrowedEvent event = new BookBorrowedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());

        String invalidJson = """
            {
              "eventId": "id",
              "eventType": "BOOK_BORROWED",
              "producer": "book-service",
              "aggregateId": "isbn",
              "occurredAt": "2026-06-09T10:00:00Z",
              "schemaVersion": 1,
              "payload": {
                "isbn": "123"
              }
            }
            """;

        return getBookContractCase(BOOK_BORROWED_EVENT_NAME, event, invalidJson);
    
    }

    private static BookContractCase bookCopiesUpdateCase() {
    	
    	BookCopiesAddedPayload payload = new BookCopiesAddedPayload("9788828606819", 2);
    	
    	BookCopiesAddedEvent event = new BookCopiesAddedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	 String invalidJson = """
    	            {
    	              "eventId": "id",
    	              "eventType": "BOOK_COPIES_UPDATED",
    	              "producer": "book-service",
    	              "aggregateId": "isbn",
    	              "occurredAt": "2026-06-09T10:00:00Z",
    	              "schemaVersion": 1,
    	              "payload": {
    	                "isbn": "123"
    	              }
    	            }
    	            """;
    	 
    	 return getBookContractCase(BOOK_COPIES_UPDATED_EVENT_NAME, event, invalidJson);
    }
    
    private static BookContractCase bookReservedCase() {
    	
    	BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	BookReservedEvent event = new BookReservedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	 String invalidJson = """
 	            {
 	              "eventId": "id",
 	              "eventType": "BOOK_RESERVED",
 	              "producer": "book-service",
 	              "aggregateId": "isbn",
 	              "occurredAt": "2026-06-09T10:00:00Z",
 	              "schemaVersion": 1,
 	              "payload": {
 	                "isbn": "123"
 	              }
 	            }
 	            """;
    	 
    	 return getBookContractCase(BOOK_RESERVED_EVENT_NAME, event, invalidJson);
    	 
    }
    
    private static BookContractCase bookReleasedCase() {
    	
    	BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	BookReleasedEvent event = new BookReleasedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	String invalidJson = """
 	            {
 	              "eventId": "id",
 	              "eventType": "BOOK_RELEASED",
 	              "producer": "book-service",
 	              "aggregateId": "isbn",
 	              "occurredAt": "2026-06-09T10:00:00Z",
 	              "schemaVersion": 1,
 	              "payload": {
 	                "isbn": "123"
 	              }
 	            }
 	            """;
    	return getBookContractCase(BOOK_RELEASED_EVENT_NAME, event, invalidJson);
    	
    }
    
    private static BookContractCase bookReturnedCase() {
    	
    	BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	BookReturnedEvent event = new BookReturnedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	String invalidJson = """
 	            {
 	              "eventId": "id",
 	              "eventType": "BOOK_RETURNED",
 	              "producer": "book-service",
 	              "aggregateId": "isbn",
 	              "occurredAt": "2026-06-09T10:00:00Z",
 	              "schemaVersion": 1,
 	              "payload": {
 	                "isbn": "123"
 	              }
 	            }
 	            """;
    	 
    	return getBookContractCase(BOOK_RETURNED_EVENT_NAME, event, invalidJson);
    	    	
    }
    
    private static BookContractCase bookReservationRejectedCase() {
    	
    	BookReservationRejectedPayload payload = new BookReservationRejectedPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", 
    			"203a9860-cdd8-4c33-a67f-45a6cf6e2799", BookReservationRejectReason.BOOK_NOT_AVAILABLE);
    	
    	BookReservationRejectedEvent event = new BookReservationRejectedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	String invalidJson = """
 	            {
 	              "eventId": "id",
 	              "eventType": "BOOK_RESERVATION_REJECTED",
 	              "producer": "book-service",
 	              "aggregateId": "isbn",
 	              "occurredAt": "2026-06-09T10:00:00Z",
 	              "schemaVersion": 1,
 	              "payload": {
 	                "isbn": "123"
 	              }
 	            }
 	            """;
    	 
    	return getBookContractCase(BOOK_RESERVATION_REJECTED_EVENT_NAME, event, invalidJson);
    	
    }
    
 private static BookContractCase bookBorrowRejectedCase() {
    	
	 BookBorrowRejectedPayload payload = new BookBorrowRejectedPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", 
    			"203a9860-cdd8-4c33-a67f-45a6cf6e2799", BookBorrowRejectReason.BOOK_NOT_REGISTERED);
    	
    	BookBorrowRejectedEvent event = new BookBorrowRejectedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	String invalidJson = """
 	            {
 	              "eventId": "id",
 	              "eventType": "BOOK_BORROW_REJECTED",
 	              "producer": "book-service",
 	              "aggregateId": "isbn",
 	              "occurredAt": "2026-06-09T10:00:00Z",
 	              "schemaVersion": 1,
 	              "payload": {
 	                "isbn": "123"
 	              }
 	            }
 	            """;
    	 
    	return getBookContractCase(BOOK_BORROW_REJECTED_EVENT_NAME, event, invalidJson);
    	
    }
 
 	private static BookContractCase getBookContractCase(String eventName, BookEvent event, String invalidJson) {
		return new BookContractCase(eventName, String.format(SCHEMA_PATH, eventName), String.format(SAMPLE_PATH, eventName), invalidJson, event);
	}
 
    private void validateSchema(String eventSchema, BookEvent bookEvent) {
        IntegrationEventEnvelope<?> integrationEvent = mapper.map(bookEvent);
        JsonNode json = objectMapper.valueToTree(integrationEvent);

        Schema schema = loadSchema(eventSchema);
        List<Error> errors = schema.validate(json);

        Assertions.assertTrue(errors.isEmpty(), errors.toString());
    }

    private void validateSample(String samplePath, String eventSchema) throws Exception {
        JsonNode sample = objectMapper.readTree(loadResource(samplePath));
        Schema schema = loadSchema(eventSchema);

        List<Error> errors = schema.validate(sample);
        Assertions.assertTrue(errors.isEmpty(), errors.toString());
    }

    private void validateInvalidJson(String invalidJson, String eventSchema) throws Exception {
        JsonNode json = objectMapper.readTree(invalidJson);
        Schema schema = loadSchema(eventSchema);

        List<Error> errors = schema.validate(json);
        Assertions.assertFalse(errors.isEmpty());
    }

}
