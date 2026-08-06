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

import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestAddedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestApprovedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestRejectedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestVotedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestAddedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestApprovedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestRejectedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestVotedPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.BookRequestIntegrationEventMapper;
import mentoring.acomi.contracts.support.JsonSchemaSupport;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class BookRequestEventProducerContractTest extends JsonSchemaSupport {

	private static final String BOOK_REQUEST_ADDED_EVENT_NAME = IntegrationEventTypes.BOOK_REQUEST_ADDED.eventName;
	private static final String BOOK_REQUEST_VOTED_EVENT_NAME = IntegrationEventTypes.BOOK_REQUEST_VOTED.eventName;
	private static final String BOOK_REQUEST_APPROVED_EVENT_NAME = IntegrationEventTypes.BOOK_REQUEST_APPROVED.eventName;
	private static final String BOOK_REQUEST_REJECTED_EVENT_NAME = IntegrationEventTypes.BOOK_REQUEST_REJECTED.eventName;
	
	private static final String SCHEMA_PATH = "contracts/bookRequest/%s/v1/event.schema.json";
	private static final String SAMPLE_PATH = "contracts/bookRequest/%s/v1/sample.json";
	
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BookRequestIntegrationEventMapper mapper = new BookRequestIntegrationEventMapper();
    
    @ParameterizedTest(name = "[{index}] valid producer event -> {0}")
    @MethodSource("validProducerCases")
    void shouldProduceValidEvent(String name, BookRequestContractCase testCase) {
        validateSchema(testCase.schemaPath(), testCase.domainEvent());
    }

    @ParameterizedTest(name = "[{index}] valid sample -> {0}")
    @MethodSource("sampleCases")
    void sampleShouldBeValid(String name, BookRequestContractCase testCase) throws Exception {
        validateSample(testCase.samplePath(), testCase.schemaPath());
    }

    @ParameterizedTest(name = "[{index}] failed invalid json -> {0}")
    @MethodSource("invalidCases")
    void shouldFailIfMissingRequiredField(String name, BookRequestContractCase testCase) throws Exception {
        validateInvalidJson(testCase.invalidJson(), testCase.schemaPath());
    }

    static Stream<Arguments> validProducerCases() {
        return Stream.of(
                Arguments.of(BOOK_REQUEST_ADDED_EVENT_NAME, bookRequestAddedCase()),
                Arguments.of(BOOK_REQUEST_VOTED_EVENT_NAME, bookRequestVotedCase()),
                Arguments.of(BOOK_REQUEST_APPROVED_EVENT_NAME, bookRequestApprovedCase()),
                Arguments.of(BOOK_REQUEST_REJECTED_EVENT_NAME, bookRequestRejectedCase())               
        );
    }

    static Stream<Arguments> sampleCases() {
        return Stream.of(
        		 Arguments.of(BOOK_REQUEST_ADDED_EVENT_NAME, bookRequestAddedCase()),
                 Arguments.of(BOOK_REQUEST_VOTED_EVENT_NAME, bookRequestVotedCase()),
                 Arguments.of(BOOK_REQUEST_APPROVED_EVENT_NAME, bookRequestApprovedCase()),
                 Arguments.of(BOOK_REQUEST_REJECTED_EVENT_NAME, bookRequestRejectedCase())
        );
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
        		 Arguments.of(BOOK_REQUEST_ADDED_EVENT_NAME, bookRequestAddedCase()),
                 Arguments.of(BOOK_REQUEST_VOTED_EVENT_NAME, bookRequestVotedCase()),
                 Arguments.of(BOOK_REQUEST_APPROVED_EVENT_NAME, bookRequestApprovedCase()),
                 Arguments.of(BOOK_REQUEST_REJECTED_EVENT_NAME, bookRequestRejectedCase())
        );
    }

    private static BookRequestContractCase bookRequestAddedCase() {
        
    	BookRequestAddedPayload payload = new BookRequestAddedPayload("9eaeae1e-fc67-4ba0-90d9-eca279666953", "Italo Calvino", 
    			"Il visconte dimezzato", "3683d58c-16ab-4bb5-9742-e2998564cf17", "9788828606819", "");

    	BookRequestAddedEvent event = new BookRequestAddedEvent("9eaeae1e-fc67-4ba0-90d9-eca279666953", "7ffda0c0-c129-4789-abc5-c2ba000edf4a", 
    			0, payload, Instant.now());

        String invalidJson = """
            {
              "eventId": "id",
              "eventType": "BOOK_REGISTERED",
              "producer": "book-service",
              "aggregateId": "isbn",
              "occurredAt": "2026-06-09T10:00:00Z",
              "schemaVersion": 1,
              "payload": {
                "requestId": ""
              }
            }
            """;

        return getBookRequestContractCase(BOOK_REQUEST_ADDED_EVENT_NAME, event, invalidJson);
    }

    private static BookRequestContractCase bookRequestVotedCase() {
    	
        BookRequestVotedPayload payload = new BookRequestVotedPayload("228473c9-d482-462b-b5b5-cf96cdb0d195", 
        		"203a9860-cdd8-4c33-a67f-45a6cf6e2799");

        BookRequestVotedEvent event = new BookRequestVotedEvent("228473c9-d482-462b-b5b5-cf96cdb0d195", "34105d72-a109-4c9c-aa0e-3e31a677d3c7",
        		0, payload, Instant.now());

        String invalidJson = """
            {
              "eventId": "id",
              "eventType": "BOOK_BORROWED",
              "producer": "book-service",
              "aggregateId": "isbn",
              "occurredAt": "2026-06-09T10:00:00Z",
              "schemaVersion": 1,
              "payload": {
                "userId": "123"
              }
            }
            """;

        return getBookRequestContractCase(BOOK_REQUEST_VOTED_EVENT_NAME, event, invalidJson);
    
    }

    private static BookRequestContractCase bookRequestApprovedCase() {
    	
    	BookRequestApprovedPayload payload = new BookRequestApprovedPayload("9f1b2fec-e9e2-4b7d-85d3-2a1f76142865");
    	
    	BookRequestApprovedEvent event = new BookRequestApprovedEvent("9f1b2fec-e9e2-4b7d-85d3-2a1f76142865", 
    			"34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	 String invalidJson = """
    	            {
    	              "eventId": "id",
    	              "eventType": "BOOK_COPIES_UPDATED",
    	              "producer": "book-service",
    	              "aggregateId": "isbn",
    	              "occurredAt": "2026-06-09T10:00:00Z",
    	              "schemaVersion": 1,
    	              "payload": {
    	                "requestId": ""
    	              }
    	            }
    	            """;
    	 
    	 return getBookRequestContractCase(BOOK_REQUEST_APPROVED_EVENT_NAME, event, invalidJson);
    }
    
    private static BookRequestContractCase bookRequestRejectedCase() {
    	
    	BookRequestRejectedPayload payload = new BookRequestRejectedPayload("79e8c74a-d945-410f-8c73-705ec42962a4", "libro presente");
    	BookRequestRejectedEvent event = new BookRequestRejectedEvent("79e8c74a-d945-410f-8c73-705ec42962a4", 
    			"34105d72-a109-4c9c-aa0e-3e31a677d3c7", 0, payload, Instant.now());
    	
    	 String invalidJson = """
 	            {
 	              "eventId": "id",
 	              "eventType": "BOOK_RESERVED",
 	              "producer": "book-service",
 	              "aggregateId": "isbn",
 	              "occurredAt": "2026-06-09T10:00:00Z",
 	              "schemaVersion": 1,
 	              "payload": {
 	                "requestId": ""
 	              }
 	            }
 	            """;
    	 
    	 return getBookRequestContractCase(BOOK_REQUEST_REJECTED_EVENT_NAME, event, invalidJson);
    	 
    }
    
   	private static BookRequestContractCase getBookRequestContractCase(String eventName, BookRequestEvent event, String invalidJson) {
		return new BookRequestContractCase(eventName, String.format(SCHEMA_PATH, eventName), String.format(SAMPLE_PATH, eventName), invalidJson, event);
	}
 
    private void validateSchema(String eventSchema, BookRequestEvent event) {
        IntegrationEventEnvelope<?> integrationEvent = mapper.map(event);
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
