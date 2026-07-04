package mentoring.acomi.bookservice.event.contract.consumer;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;

import mentoring.acomi.contracts.support.JsonSchemaSupport;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


public class LoanEventConsumerContractTest extends JsonSchemaSupport {
	
	private static final String LOAN_RETURNED_EVENT_NAME = IntegrationEventTypes.LOAN_RETURNED.eventName;
	private static final String LOAN_CANCELED_EVENT_NAME = IntegrationEventTypes.LOAN_CANCELED.eventName;
	private static final String LOAN_CONFIRM_REQUESTED_EVENT_NAME =  IntegrationEventTypes.LOAN_CONFIRM_REQUESTED.eventName;
	private static final String LOAN_REQUESTED_EVENT_NAME = IntegrationEventTypes.LOAN_REQUESTED.eventName;
	
	private static final String LOAN_SCHEMA_PATH = "contracts/loan/%s/v1/event.schema.json";
	private static final String LOAN_SAMPLE_PATH = "contracts/loan/%s/v1/sample.json";
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	@ParameterizedTest(name = "[{index}] valid sample -> {0}")
	@MethodSource("sampleCases")
	void sampleShouldBeValid(String name, LoanContractCase testCase) throws Exception {
		validateSample(testCase.samplePath(), testCase.schemaPath());
	}
	
	@Test
	void shouldDeserializeLoanRequestedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, LOAN_REQUESTED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

		Assertions.assertEquals("LOAN_REQUESTED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeLoanConfirmRequestedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, LOAN_CONFIRM_REQUESTED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

		Assertions.assertEquals("LOAN_CONFIRM_REQUESTED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeLoanCanceledEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, LOAN_CANCELED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

		Assertions.assertEquals("LOAN_CANCELED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeLoanReturnedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, LOAN_RETURNED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

		Assertions.assertEquals("LOAN_RETURNED", event.eventType().toString());
	}
	
	private void validateSample(String samplePath, String eventSchema) throws Exception {
		JsonNode sample = objectMapper.readTree(loadResource(samplePath));
		Schema schema = loadSchema(eventSchema);

		List<Error> errors = schema.validate(sample);
		Assertions.assertTrue(errors.isEmpty(), errors.toString());
	}

	static Stream<Arguments> sampleCases() {
		return Stream.of(Arguments.of(LOAN_REQUESTED_EVENT_NAME, getLoanContractCase(LOAN_REQUESTED_EVENT_NAME)),
				Arguments.of(LOAN_CONFIRM_REQUESTED_EVENT_NAME, getLoanContractCase(LOAN_CONFIRM_REQUESTED_EVENT_NAME)),
				Arguments.of(LOAN_CANCELED_EVENT_NAME, getLoanContractCase(LOAN_CANCELED_EVENT_NAME)),
				Arguments.of(LOAN_RETURNED_EVENT_NAME, getLoanContractCase(LOAN_RETURNED_EVENT_NAME))
		);
	}

	private static LoanContractCase getLoanContractCase(String eventName) {
		return new LoanContractCase(eventName, String.format(LOAN_SCHEMA_PATH, eventName), String.format(LOAN_SAMPLE_PATH, eventName));
	}

}
