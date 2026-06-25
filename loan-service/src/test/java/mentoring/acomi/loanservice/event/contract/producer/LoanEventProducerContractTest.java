package mentoring.acomi.loanservice.event.contract.producer;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmedEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedEvent;
import mentoring.acomi.loanservice.domain.events.LoanFailedReason;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReservedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReturnedEvent;
import mentoring.acomi.loanservice.domain.events.payload.LoanFailedPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.domain.model.DateRange;
import mentoring.acomi.loanservice.domain.model.LoanStatus;
import mentoring.acomi.loanservice.infrastructure.messaging.LoanIntegrationEventMapper;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;

public class LoanEventProducerContractTest {

	private static final String LOAN_FAILED_EVENT_NAME = IntegrationEventTypes.LOAN_FAILED.eventName;
	private static final String LOAN_RESERVED_EVENT_NAME = IntegrationEventTypes.LOAN_RESERVED.eventName;
	private static final String LOAN_RETURNED_EVENT_NAME = IntegrationEventTypes.LOAN_RETURNED.eventName;
	private static final String LOAN_CANCELED_EVENT_NAME = IntegrationEventTypes.LOAN_CANCELED.eventName;
	private static final String LOAN_CONFIRM_REQUESTED_EVENT_NAME = IntegrationEventTypes.LOAN_CONFIRM_REQUESTED.eventName;
	private static final String LOAN_CONFIRMED_EVENT_NAME = IntegrationEventTypes.LOAN_CONFIRMED.eventName;
	private static final String LOAN_REQUESTED_EVENT_NAME = IntegrationEventTypes.LOAN_REQUESTED.eventName;
	
	private static final String SCHEMA_PATH = "contracts/loan/%s/v1/event.schema.json";
	private static final String SAMPLE_PATH = "contracts/loan/%s/v1/sample.json";

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private final LoanIntegrationEventMapper mapper = new LoanIntegrationEventMapper();

	@ParameterizedTest(name = "[{index}] valid producer event -> {0}")
	@MethodSource("validProducerCases")
	void shouldProduceValidEvent(String name, LoanContractCase testCase) {
		validateSchema(testCase.schemaPath(), testCase.domainEvent());
	}

	@ParameterizedTest(name = "[{index}] valid sample -> {0}")
	@MethodSource("sampleCases")
	void sampleShouldBeValid(String name, LoanContractCase testCase) throws Exception {
		validateSample(testCase.samplePath(), testCase.schemaPath());
	}

	@ParameterizedTest(name = "[{index}] failed invalid json -> {0}")
	@MethodSource("invalidCases")
	void shouldFailIfMissingRequiredField(String name, LoanContractCase testCase) throws Exception {
		validateInvalidJson(testCase.invalidJson(), testCase.schemaPath());
	}

	static Stream<Arguments> validProducerCases() {
        return Stream.of(
                Arguments.of(LOAN_REQUESTED_EVENT_NAME,loanRequestedCase()),
                Arguments.of(LOAN_CONFIRMED_EVENT_NAME, loanConfirmedCase()),
                Arguments.of(LOAN_CONFIRM_REQUESTED_EVENT_NAME, loanConfirmRequestedCase()),
                Arguments.of(LOAN_CANCELED_EVENT_NAME, loanCanceledCase()),
                Arguments.of(LOAN_RETURNED_EVENT_NAME, loanReturned()),
                Arguments.of(LOAN_RESERVED_EVENT_NAME, loanReservedCase()),
                Arguments.of(LOAN_FAILED_EVENT_NAME, loanFailedCase())              
        );
    }

	static Stream<Arguments> sampleCases() {
    	return Stream.of(
                Arguments.of(LOAN_REQUESTED_EVENT_NAME,loanRequestedCase()),
                Arguments.of(LOAN_CONFIRMED_EVENT_NAME, loanConfirmedCase()),
                Arguments.of(LOAN_CONFIRM_REQUESTED_EVENT_NAME, loanConfirmRequestedCase()),
                Arguments.of(LOAN_CANCELED_EVENT_NAME, loanCanceledCase()),
                Arguments.of(LOAN_RETURNED_EVENT_NAME, loanReturned()),
                Arguments.of(LOAN_RESERVED_EVENT_NAME, loanReservedCase()),
                Arguments.of(LOAN_FAILED_EVENT_NAME, loanFailedCase())             
        );
    }

    static Stream<Arguments> invalidCases() {
    	return Stream.of(
                Arguments.of(LOAN_REQUESTED_EVENT_NAME,loanRequestedCase()),
                Arguments.of(LOAN_CONFIRMED_EVENT_NAME, loanConfirmedCase()),
                Arguments.of(LOAN_CONFIRM_REQUESTED_EVENT_NAME, loanConfirmRequestedCase()),
                Arguments.of(LOAN_CANCELED_EVENT_NAME, loanCanceledCase()),
                Arguments.of(LOAN_RETURNED_EVENT_NAME, loanReturned()),
                Arguments.of(LOAN_RESERVED_EVENT_NAME, loanReservedCase()),
                Arguments.of(LOAN_FAILED_EVENT_NAME, loanFailedCase())            
        );
    }
    
    private static LoanContractCase loanFailedCase() {
    	LoanFailedPayload payload = new LoanFailedPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", LoanFailedReason.BOOK_NOT_FOUND);
    	LoanFailedEvent event = new LoanFailedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "d4cc156e-04f8-4fa8-866e-359515bcc104", 0, payload, Instant.now());
		
    	String invalidJson = """
	            {
	              "eventId": "d4cc156e-04f8-4fa8-866e-359515bcc104",
	              "eventType": "LOAN_FAILED",
	              "producer": "loan-service",
	              "aggregateId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	              "occurredAt": "2026-06-09T10:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"loanId": "1b21387f-12a8-40a0-8e6a-605890bda1b0"
	              }
	            }
	            """;
    	
    	return getLoanContractCase(LOAN_FAILED_EVENT_NAME, event, invalidJson);
	}

	private static LoanContractCase loanReservedCase() {
    	LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
    	LoanReservedEvent event = new LoanReservedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "f1972050-5803-47e8-9c4c-1f8d46716600", 0, payload, Instant.now());
		
    	String invalidJson = """
	            {
	              "eventId": "f1972050-5803-47e8-9c4c-1f8d46716600",
	              "eventType": "LOAN_RESERVED",
	              "producer": "loan-service",
	              "aggregateId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	              "occurredAt": "2026-06-09T10:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"loanId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	                "isbn": "9788828606819"
	              }
	            }
	            """;
    	
    	return getLoanContractCase(LOAN_RESERVED_EVENT_NAME, event, invalidJson);
	}

	private static LoanContractCase loanReturned() {
    	LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
    	LoanReturnedEvent event = new LoanReturnedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "d282a0bd-0bec-4257-861d-c1585e0a0e93", 0, payload, Instant.now());
		
    	String invalidJson = """
	            {
	              "eventId": "d282a0bd-0bec-4257-861d-c1585e0a0e93",
	              "eventType": "LOAN_RETURNED",
	              "producer": "loan-service",
	              "aggregateId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	              "occurredAt": "2026-06-09T10:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"loanId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	                "isbn": "9788828606819"
	              }
	            }
	            """;
    	
    	return getLoanContractCase(LOAN_RETURNED_EVENT_NAME, event, invalidJson);
	}

	private static LoanContractCase loanCanceledCase() {
    	LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
    	LoanCanceledEvent event = new LoanCanceledEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "046f099d-41f9-4c05-8da9-470b789f6a3b", 0, payload, Instant.now());
		
    	String invalidJson = """
	            {
	              "eventId": "046f099d-41f9-4c05-8da9-470b789f6a3b",
	              "eventType": "LOAN_CANCELED",
	              "producer": "loan-service",
	              "aggregateId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	              "occurredAt": "2026-06-09T10:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"loanId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	                "isbn": "9788828606819"
	              }
	            }
	            """;
    	
    	return getLoanContractCase(LOAN_CANCELED_EVENT_NAME, event, invalidJson);
	}

	private static LoanContractCase loanConfirmRequestedCase() {
    	LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
    	LoanConfirmRequestedEvent event = new LoanConfirmRequestedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "355d2c7b-dc0d-4b8a-b27f-acc8375eeaee", 0, 
    			payload, Instant.now());
		
    	String invalidJson = """
	            {
	              "eventId": "355d2c7b-dc0d-4b8a-b27f-acc8375eeaee",
	              "eventType": "LOAN_CONFIRM_REQUESTED",
	              "producer": "loan-service",
	              "aggregateId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	              "occurredAt": "2026-06-09T10:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"loanId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	                "isbn": "9788828606819"
	              }
	            }
	            """;
    	
    	return getLoanContractCase(LOAN_CONFIRM_REQUESTED_EVENT_NAME, event, invalidJson);
	}

	private static LoanContractCase loanConfirmedCase() {
    	LoanPayload payload = new LoanPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b");
    	LoanConfirmedEvent event = new LoanConfirmedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "ebe0e803-afb9-4998-beb0-200773e7c764", 0, payload, Instant.now());
		
    	String invalidJson = """
	            {
	              "eventId": "ebe0e803-afb9-4998-beb0-200773e7c764",
	              "eventType": "LOAN_CONFIRMED",
	              "producer": "loan-service",
	              "aggregateId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	              "occurredAt": "2026-06-09T10:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"loanId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	                "isbn": "9788828606819"
	              }
	            }
	            """;
    	
    	return getLoanContractCase(LOAN_CONFIRMED_EVENT_NAME, event, invalidJson);
	}
    
	private static LoanContractCase loanRequestedCase() {
		LoanRequestPayload payload = new LoanRequestPayload("1b21387f-12a8-40a0-8e6a-605890bda1b0", "9788828606819", "2ce6d405-d3fc-4042-b06e-cf5efc4cc65b", 
				new DateRange(LocalDate.now(), null, Clock.systemUTC()), LoanStatus.PENDING);
		LoanRequestedEvent event = new LoanRequestedEvent("1b21387f-12a8-40a0-8e6a-605890bda1b0", "3d209d01-4b1d-4d64-991f-d0a63a7ddecd", 0, payload, Instant.now());
		
		String invalidJson = """
	            {
	              "eventId": "3d209d01-4b1d-4d64-991f-d0a63a7ddecd",
	              "eventType": "LOAN_REQUESTED",
	              "producer": "loan-service",
	              "aggregateId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	              "occurredAt": "2026-06-09T10:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"loanId": "1b21387f-12a8-40a0-8e6a-605890bda1b0",
	                "isbn": "9788828606819"
	              }
	            }
	            """;
		
		return getLoanContractCase(LOAN_REQUESTED_EVENT_NAME, event, invalidJson);
	}

	private static LoanContractCase getLoanContractCase(String eventName, LoanEvent event, String invalidJson) {
		return new LoanContractCase(eventName, String.format(SCHEMA_PATH, eventName), String.format(SAMPLE_PATH, eventName), invalidJson, event);
	}

	private void validateSchema(String eventSchema, LoanEvent loanEvent) {
		IntegrationEventEnvelope<?> integrationEvent = mapper.map(loanEvent);
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

	private void validateInvalidJson(String invalidJson, String eventSchema) throws Exception {
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
