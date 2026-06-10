package mentoring.acomi.bookservice.event.contract.consumer;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import mentoring.acomi.bookservice.infrastructure.messaging.payload.consumer.LoanIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;

public class LoanEventConsumerContractTest {
	
	private static final String LOAN_SCHEMA_PATH = "contracts/loan/%s/v1/event.schema.json";
	private static final String LOAN_SAMPLE_PATH = "contracts/loan/%s/v1/sample.json";
	
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	@ParameterizedTest(name = "[{index}] valid sample -> {0}")
	@MethodSource("sampleCases")
	void sampleShouldBeValid(String name, LoanContractCase testCase) throws Exception {
		validateSample(testCase.samplePath(), testCase.schemaPath());
	}
	
	@Test
	void shouldDeserializeLoanRequestedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, "loan.requested")));

		IntegrationEventEnvelope<LoanIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("LOAN_REQUESTED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeLoanConfirmRequestedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, "loan.confirm.requested")));

		IntegrationEventEnvelope<LoanIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("LOAN_CONFIRM_REQUESTED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeLoanCanceledEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, "loan.canceled")));

		IntegrationEventEnvelope<LoanIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("LOAN_CANCELED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeLoanReturnedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(LOAN_SAMPLE_PATH, "loan.returned")));

		IntegrationEventEnvelope<LoanIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("LOAN_RETURNED", event.eventType().toString());
	}
	
	private void validateSample(String samplePath, String eventSchema) throws Exception {
		JsonNode sample = objectMapper.readTree(loadResource(samplePath));
		JsonSchema schema = loadSchema(eventSchema);

		Set<ValidationMessage> errors = schema.validate(sample);
		Assertions.assertTrue(errors.isEmpty(), errors.toString());
	}

	static Stream<Arguments> sampleCases() {
		return Stream.of(Arguments.of("loan.requested", getLoanContractCase("loan.requested")),
				Arguments.of("loan.confirm.requested", getLoanContractCase("loan.confirm.requested")),
				Arguments.of("loan.canceled", getLoanContractCase("loan.canceled")),
				Arguments.of("loan.returned", getLoanContractCase("loan.returned"))
		);
	}

	private static LoanContractCase getLoanContractCase(String eventName) {
		return new LoanContractCase(eventName, String.format(LOAN_SCHEMA_PATH, eventName), String.format(LOAN_SAMPLE_PATH, eventName));
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
