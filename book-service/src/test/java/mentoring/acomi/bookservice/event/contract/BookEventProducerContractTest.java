package mentoring.acomi.bookservice.event.contract;

import java.io.InputStream;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.SpecVersion;

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
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class BookEventProducerContractTest {

	private static final String SCHEMA_PATH = "contracts/book/%s/v1/event.schema.json";
	private static final String SAMPLE_PATH = "contracts/book/%s/v1/sample.json";
	
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
                Arguments.of("book.registered", bookRegisteredCase()),
                Arguments.of("book.borrowed", bookBorrowedCase()),
                Arguments.of("book.copies.updated", bookCopiesUpdateCase()),
                Arguments.of("book.reserved", bookReservedCase()),
                Arguments.of("book.released", bookReleasedCase()),
                Arguments.of("book.returned", bookReturnedCase()),
                Arguments.of("book.reservation.rejected", bookReservationRejectedCase()),
                Arguments.of("book.borrow.rejected", bookBorrowRejectedCase())
               
        );
    }

    static Stream<Arguments> sampleCases() {
        return Stream.of(
                Arguments.of("book.registered", bookRegisteredCase()),
                Arguments.of("book.borrowed", bookBorrowedCase()),
                Arguments.of("book.copies.updated", bookCopiesUpdateCase()),
                Arguments.of("book.reserved", bookReservedCase()),
                Arguments.of("book.released", bookReleasedCase()),
                Arguments.of("book.returned", bookReturnedCase()),
                Arguments.of("book.reservation.rejected", bookReservationRejectedCase()),
                Arguments.of("book.borrow.rejected", bookBorrowRejectedCase())
              
        );
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of("book.registered", bookRegisteredCase()),
                Arguments.of("book.borrowed", bookBorrowedCase()),
                Arguments.of("book.copies.updated", bookCopiesUpdateCase()),
                Arguments.of("book.reserved", bookReservedCase()),
                Arguments.of("book.released", bookReleasedCase()),
                Arguments.of("book.returned", bookReturnedCase()),
                Arguments.of("book.reservation.rejected", bookReservationRejectedCase()),
                Arguments.of("book.borrow.rejected", bookBorrowRejectedCase())
        );
    }

    private static BookContractCase bookRegisteredCase() {
        
    	BookRegisteredPayload payload = new BookRegisteredPayload("9788828606819", "Italo Calvino", "Il visconte dimezzato", "Trilogia");

        BookRegisteredEvent event = new BookRegisteredEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c6", payload, Instant.now());

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

        return getBookContractCase("book.registered", event, invalidJson);
    }

    private static BookContractCase bookBorrowedCase() {
    	
        BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");

        BookBorrowedEvent event = new BookBorrowedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", payload, Instant.now());

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

        return getBookContractCase("book.borrowed", event, invalidJson);
    
    }

    private static BookContractCase bookCopiesUpdateCase() {
    	
    	BookCopiesAddedPayload payload = new BookCopiesAddedPayload("9788828606819", 2);
    	
    	BookCopiesAddedEvent event = new BookCopiesAddedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", payload, Instant.now());
    	
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
    	 
    	 return getBookContractCase("book.copies.updated", event, invalidJson);
    }
    
    private static BookContractCase bookReservedCase() {
    	
    	BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	BookReservedEvent event = new BookReservedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", payload, Instant.now());
    	
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
    	 
    	 return getBookContractCase("book.reserved", event, invalidJson);
    	 
    }
    
    private static BookContractCase bookReleasedCase() {
    	
    	BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	BookReleasedEvent event = new BookReleasedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", payload, Instant.now());
    	
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
    	return getBookContractCase("book.released", event, invalidJson);
    	
    }
    
    private static BookContractCase bookReturnedCase() {
    	
    	BookLoanPayload payload = new BookLoanPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", "203a9860-cdd8-4c33-a67f-45a6cf6e2799");
    	BookReturnedEvent event = new BookReturnedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", payload, Instant.now());
    	
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
    	 
    	return getBookContractCase("book.returned", event, invalidJson);
    	    	
    }
    
    private static BookContractCase bookReservationRejectedCase() {
    	
    	BookReservationRejectedPayload payload = new BookReservationRejectedPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", 
    			"203a9860-cdd8-4c33-a67f-45a6cf6e2799", BookReservationRejectReason.BOOK_NOT_AVAILABLE);
    	
    	BookReservationRejectedEvent event = new BookReservationRejectedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", payload, Instant.now());
    	
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
    	 
    	return getBookContractCase("book.reservation.rejected", event, invalidJson);
    	
    }
    
 private static BookContractCase bookBorrowRejectedCase() {
    	
	 BookBorrowRejectedPayload payload = new BookBorrowRejectedPayload("9788828606819", "228473c9-d482-462b-b5b5-cf96cdb0d195", 
    			"203a9860-cdd8-4c33-a67f-45a6cf6e2799", BookBorrowRejectReason.BOOK_NOT_REGISTERED);
    	
    	BookBorrowRejectedEvent event = new BookBorrowRejectedEvent("9788828606819", "34105d72-a109-4c9c-aa0e-3e31a677d3c7", payload, Instant.now());
    	
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
    	 
    	return getBookContractCase("book.borrow.rejected", event, invalidJson);
    	
    }
 
 	private static BookContractCase getBookContractCase(String eventName, BookEvent event, String invalidJson) {
		return new BookContractCase(eventName, String.format(SCHEMA_PATH, eventName), String.format(SAMPLE_PATH, eventName), invalidJson, event);
	}
 
    private void validateSchema(String eventSchema, BookEvent bookEvent) {
        IntegrationEventEnvelope<?> integrationEvent = mapper.map(bookEvent);
        JsonNode json = objectMapper.valueToTree(integrationEvent);

        JsonSchema schema = loadSchema(eventSchema);
        Set<ValidationMessage> errors = schema.validate(json);

        Assertions.assertTrue(errors.isEmpty(), errors.toString());
    }

    private void validateSample(String samplePath, String eventSchema) throws Exception {
        JsonNode sample = objectMapper.readTree(loadResource(samplePath));
        JsonSchema schema = loadSchema(eventSchema);

        Set<ValidationMessage> errors = schema.validate(sample);
        Assertions.assertTrue(errors.isEmpty(), errors.toString());
    }

    protected void validateInvalidJson(String invalidJson, String eventSchema) throws Exception {
        JsonNode json = objectMapper.readTree(invalidJson);
        JsonSchema schema = loadSchema(eventSchema);

        Set<ValidationMessage> errors = schema.validate(json);
        Assertions.assertFalse(errors.isEmpty());
    }

    private JsonSchema loadSchema(String path) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        return factory.getSchema(loadResource(path));
    }

    private InputStream loadResource(String path) {
        
    	InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new IllegalStateException(String.format("File not found in classpath: %s", path));
        }

        return is;
    }
}
