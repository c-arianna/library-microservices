package mentoring.acomi.loanservice.event.contract.consumer.user;

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

import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;

public class UserEventConsumerContractTest {

	private static final String USER_UNSUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_UNSUSPENDED.eventName;
	private static final String USER_SUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_SUSPENDED.eventName;
	private static final String USER_UNSUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_UNSUBSCRIBED.eventName;
	private static final String USER_SUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_SUBSCRIBED.eventName;
	
	private static final String USER_SCHEMA_PATH = "contracts/user/%s/v1/event.schema.json";
	private static final String USER_SAMPLE_PATH = "contracts/user/%s/v1/sample.json";
	
	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	
	@ParameterizedTest(name = "[{index}] valid sample -> {0}")
	@MethodSource("sampleCases")
	void sampleShouldBeValid(String name, UserContractCase testCase) throws Exception {
		validateSample(testCase.samplePath(), testCase.schemaPath());
	}
	
	@Test
	void shouldDeserializeUserSubscribedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_SUBSCRIBED_EVENT_NAME)));

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("USER_SUBSCRIBED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeUserUnsubscribedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_UNSUBSCRIBED_EVENT_NAME)));

		IntegrationEventEnvelope<UserIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("USER_UNSUBSCRIBED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeUserSuspendedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_SUSPENDED_EVENT_NAME)));

		IntegrationEventEnvelope<UserIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("USER_SUSPENDED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeUserUnsuspendedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_UNSUSPENDED_EVENT_NAME)));

		IntegrationEventEnvelope<UserIntegrationPayload> event = objectMapper.treeToValue(json, new TypeReference<>() {});

		Assertions.assertEquals("USER_UNSUSPENDED", event.eventType().toString());
	}
	
	static Stream<Arguments> sampleCases() {
		return Stream.of(Arguments.of(USER_SUBSCRIBED_EVENT_NAME, getUserContractCase(USER_SUBSCRIBED_EVENT_NAME)),
				Arguments.of(USER_UNSUBSCRIBED_EVENT_NAME, getUserContractCase(USER_UNSUBSCRIBED_EVENT_NAME)),
				Arguments.of(USER_SUSPENDED_EVENT_NAME, getUserContractCase(USER_SUSPENDED_EVENT_NAME)),
				Arguments.of(USER_UNSUSPENDED_EVENT_NAME, getUserContractCase(USER_UNSUSPENDED_EVENT_NAME))
		);
	}
	
	private static UserContractCase getUserContractCase(String eventName) {
		return new UserContractCase(eventName, String.format(USER_SCHEMA_PATH, eventName), String.format(USER_SAMPLE_PATH, eventName));
	}
	
	private void validateSample(String samplePath, String eventSchema) throws Exception {
		JsonNode sample = objectMapper.readTree(loadResource(samplePath));
		JsonSchema schema = loadSchema(eventSchema);

		Set<ValidationMessage> errors = schema.validate(sample);
		Assertions.assertTrue(errors.isEmpty(), errors.toString());
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
