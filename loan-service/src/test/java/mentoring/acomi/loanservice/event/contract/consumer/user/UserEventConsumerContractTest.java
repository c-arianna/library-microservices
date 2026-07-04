package mentoring.acomi.loanservice.event.contract.consumer.user;

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

public class UserEventConsumerContractTest extends JsonSchemaSupport {

	private static final String USER_UNSUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_UNSUSPENDED.eventName;
	private static final String USER_SUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_SUSPENDED.eventName;
	private static final String USER_UNSUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_UNSUBSCRIBED.eventName;
	private static final String USER_SUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_SUBSCRIBED.eventName;
	
	private static final String USER_SCHEMA_PATH = "contracts/user/%s/v1/event.schema.json";
	private static final String USER_SAMPLE_PATH = "contracts/user/%s/v1/sample.json";
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@ParameterizedTest(name = "[{index}] valid sample -> {0}")
	@MethodSource("sampleCases")
	void sampleShouldBeValid(String name, UserContractCase testCase) throws Exception {
		validateSample(testCase.samplePath(), testCase.schemaPath());
	}
	
	@Test
	void shouldDeserializeUserSubscribedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_SUBSCRIBED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

		Assertions.assertEquals("USER_SUBSCRIBED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeUserUnsubscribedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_UNSUBSCRIBED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

		Assertions.assertEquals("USER_UNSUBSCRIBED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeUserSuspendedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_SUSPENDED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

		Assertions.assertEquals("USER_SUSPENDED", event.eventType().toString());
	}
	
	@Test
	void shouldDeserializeUserUnsuspendedEvent() throws Exception {
		JsonNode json = objectMapper.readTree(loadResource(String.format(USER_SAMPLE_PATH, USER_UNSUSPENDED_EVENT_NAME)));

		IntegrationEventEnvelope<?> event = objectMapper.treeToValue(json, IntegrationEventEnvelope.class);

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
		Schema schema = loadSchema(eventSchema);

		List<Error> errors = schema.validate(sample);
		Assertions.assertTrue(errors.isEmpty(), errors.toString());
	}
	
}
