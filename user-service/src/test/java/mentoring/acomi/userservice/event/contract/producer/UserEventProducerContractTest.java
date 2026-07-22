package mentoring.acomi.userservice.event.contract.producer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;

import mentoring.acomi.contracts.support.JsonSchemaSupport;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.domain.events.LibraryCardAssignedEvent;
import mentoring.acomi.userservice.domain.events.UserSubscribedEvent;
import mentoring.acomi.userservice.domain.events.UserSuspendEvent;
import mentoring.acomi.userservice.domain.events.UserUnsubscribeEvent;
import mentoring.acomi.userservice.domain.events.UserUnsuspendedEvent;
import mentoring.acomi.userservice.domain.events.payload.LibraryCardAssignedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserPayload;
import mentoring.acomi.userservice.domain.events.payload.UserSubscribedPayload;
import mentoring.acomi.userservice.domain.events.payload.UserUnsubscribedPayload;
import mentoring.acomi.userservice.infrastructure.messaging.UserIntegrationEventMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class UserEventProducerContractTest extends JsonSchemaSupport {
	
	private static final String MAIL = "test@gmail.com";
	private static final String USER_ID = "148a2b0c-1c3c-4e81-b522-5c4a07f71a9a";
	private static final String USER_UNSUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_UNSUSPENDED.eventName;
	private static final String USER_SUSPENDED_EVENT_NAME = IntegrationEventTypes.USER_SUSPENDED.eventName;
	private static final String USER_UNSUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_UNSUBSCRIBED.eventName;
	private static final String USER_SUBSCRIBED_EVENT_NAME = IntegrationEventTypes.USER_SUBSCRIBED.eventName;
	private static final String LIBRARY_CARD_ASSIGNED_EVENT_NAME = IntegrationEventTypes.LIBRARY_CARD_ASSIGNED.eventName;
	
	private static final String SCHEMA_PATH = "contracts/user/%s/v%d/event.schema.json";
	private static final String SAMPLE_PATH = "contracts/user/%s/v%d/sample.json";
	
	private static final int SCHEMA_VERSION_1 = 1;
	private static final int SCHEMA_VERSION_2 = 2;
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final UserIntegrationEventMapper mapper = new UserIntegrationEventMapper();

	@ParameterizedTest(name = "[{index}] valid producer event -> {0}")
	@MethodSource("validProducerCases")
	void shouldProduceValidEvent(String name, UserContractCase testCase) {
		validateSchema(testCase.schemaPath(), testCase.domainEvent());
	}

	@ParameterizedTest(name = "[{index}] valid sample -> {0}")
	@MethodSource("sampleCases")
	void sampleShouldBeValid(String name, UserContractCase testCase) throws Exception {
		validateSample(testCase.samplePath(), testCase.schemaPath());
	}

	@ParameterizedTest(name = "[{index}] failed invalid json -> {0}")
	@MethodSource("invalidCases")
	void shouldFailIfMissingRequiredField(String name, UserContractCase testCase) throws Exception {
		validateInvalidJson(testCase.invalidJson(), testCase.schemaPath());
	}
	
	static Stream<Arguments> validProducerCases() {
        return Stream.of(
                Arguments.of(USER_SUBSCRIBED_EVENT_NAME, userSubscribedCaseV1()),
                Arguments.of(USER_UNSUBSCRIBED_EVENT_NAME, userUnsubscribedCaseV1()),
                Arguments.of(USER_SUSPENDED_EVENT_NAME, userSuspendedCaseV1()),
                Arguments.of(USER_UNSUSPENDED_EVENT_NAME, userUnsuspendedCaseV1()),
                Arguments.of(LIBRARY_CARD_ASSIGNED_EVENT_NAME, libraryCardAssignedCaseV1()),
                Arguments.of(USER_SUBSCRIBED_EVENT_NAME, userSubscribedCaseV2())
        );
    }

	static Stream<Arguments> sampleCases() {
		 return Stream.of(
	                Arguments.of(USER_SUBSCRIBED_EVENT_NAME, userSubscribedCaseV1()),
	                Arguments.of(USER_UNSUBSCRIBED_EVENT_NAME, userUnsubscribedCaseV1()),
	                Arguments.of(USER_SUSPENDED_EVENT_NAME, userSuspendedCaseV1()),
	                Arguments.of(USER_UNSUSPENDED_EVENT_NAME, userUnsuspendedCaseV1()),
	                Arguments.of(LIBRARY_CARD_ASSIGNED_EVENT_NAME, libraryCardAssignedCaseV1()),
	                Arguments.of(USER_SUBSCRIBED_EVENT_NAME, userSubscribedCaseV2())
	        );
    }

    static Stream<Arguments> invalidCases() {
    	 return Stream.of(
                 Arguments.of(USER_SUBSCRIBED_EVENT_NAME, userSubscribedCaseV1()),
                 Arguments.of(USER_UNSUBSCRIBED_EVENT_NAME, userUnsubscribedCaseV1()),
                 Arguments.of(USER_SUSPENDED_EVENT_NAME, userSuspendedCaseV1()),
                 Arguments.of(USER_UNSUSPENDED_EVENT_NAME, userUnsuspendedCaseV1()),
                 Arguments.of(LIBRARY_CARD_ASSIGNED_EVENT_NAME, libraryCardAssignedCaseV1()),
                 Arguments.of(USER_SUBSCRIBED_EVENT_NAME, userSubscribedCaseV2())
         );
    }
    
	private static UserContractCase userUnsuspendedCaseV1() {
		UserPayload payload = new UserPayload(USER_ID, MAIL, "Policy Violation", "admin1");
		UserUnsuspendedEvent event = new UserUnsuspendedEvent(USER_ID, "d50cc664-0391-4833-8414-18c4c9e1bd45", 0, payload, Instant.now());
		
		String invalidJson = """
	            {
	              "eventId": "d50cc664-0391-4833-8414-18c4c9e1bd45",
	              "eventType": "USER_UNSUSPENDED",
	              "producer": "user-service",
	              "aggregateId": "%s",
	              "occurredAt": "2026-06-10T11:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"id": "%s",
	                "mail": "%s"
	              }
	            }
	            """.formatted(USER_ID, USER_ID, MAIL);
		
		return getUserContractCase(USER_UNSUSPENDED_EVENT_NAME, event, invalidJson, SCHEMA_VERSION_1);
	}

	private static UserContractCase userSuspendedCaseV1() {
		UserPayload payload = new UserPayload(USER_ID, MAIL, "Policy Violation", "admin1");
		UserSuspendEvent event = new UserSuspendEvent(USER_ID, "92e57621-d491-4c02-9a21-d910107e71d0", 0, payload, Instant.now());
		
		String invalidJson = """
	            {
	              "eventId": "92e57621-d491-4c02-9a21-d910107e71d0",
	              "eventType": "USER_SUSPENDED",
	              "producer": "user-service",
	              "aggregateId": "%s",
	              "occurredAt": "2026-06-10T11:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"id": "%s",
	                "mail": "%s"
	              }
	            }
	            """.formatted(USER_ID, USER_ID, MAIL);
		
		return getUserContractCase(USER_SUSPENDED_EVENT_NAME, event, invalidJson, SCHEMA_VERSION_1);
	}

	private static UserContractCase userUnsubscribedCaseV1() {
		UserUnsubscribedPayload payload = new UserUnsubscribedPayload(USER_ID, MAIL, "Unsubscribed");
		UserUnsubscribeEvent event = new UserUnsubscribeEvent(USER_ID, "9c8db1b1-38d0-4717-aa34-702365299081", 0, payload, Instant.now()); 
		
		String invalidJson = """
	            {
	              "eventId": "9c8db1b1-38d0-4717-aa34-702365299081",
	              "eventType": "USER_UNSUBSCRIBED",
	              "producer": "user-service",
	              "aggregateId": "%s",
	              "occurredAt": "2026-06-10T11:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"id": "%s",
	                "mail": "%s"
	              }
	            }
	            """.formatted(USER_ID, USER_ID, MAIL);
		
		return getUserContractCase(USER_UNSUBSCRIBED_EVENT_NAME, event, invalidJson, SCHEMA_VERSION_1);
	}

	private static UserContractCase userSubscribedCaseV1() {
		String identityId = UUID.randomUUID().toString();
		
		UserSubscribedPayload payload = new UserSubscribedPayload(USER_ID, MAIL, "Test", 
				"Test", identityId, null, UserStatus.ACTIVE, UserRole.READER);
		UserSubscribedEvent event = new UserSubscribedEvent(USER_ID, "07bf89ea-fd4e-4080-8fda-eb94679c4f6d", 0, payload, Instant.now());
		
		String invalidJson = """
	            {
	              "eventId": "07bf89ea-fd4e-4080-8fda-eb94679c4f6d",
	              "eventType": "USER_SUBSCRIBED",
	              "producer": "user-service",
	              "aggregateId": "%s",
	              "occurredAt": "2026-06-10T11:00:00Z",
	              "schemaVersion": 1,
	              "payload": {
	              	"id": "%s",
	                "mail": "%s"
	              }
	            }
	            """.formatted(USER_ID, USER_ID, MAIL);
		
		return getUserContractCase(USER_SUBSCRIBED_EVENT_NAME, event, invalidJson, SCHEMA_VERSION_1);
	}
	
	private static UserContractCase userSubscribedCaseV2() {
		
		String identityId = UUID.randomUUID().toString();
		
		UserSubscribedPayload payload = new UserSubscribedPayload(USER_ID, MAIL, "Test", 
				"Test", identityId, "LIB-000001", UserStatus.ACTIVE, UserRole.READER);
		UserSubscribedEvent event = new UserSubscribedEvent(USER_ID, "07bf89ea-fd4e-4080-8fda-eb94679c4f6d", 0, payload, Instant.now());
		
		String invalidJson = """
	            {
	              "eventId": "07bf89ea-fd4e-4080-8fda-eb94679c4f6d",
	              "eventType": "USER_SUBSCRIBED",
	              "producer": "user-service",
	              "aggregateId": "%s",
	              "occurredAt": "2026-06-10T11:00:00Z",
	              "schemaVersion": 2,
	              "payload": {
	              	"id": "%s",
	                "mail": "%s"
	              }
	            }
	            """.formatted(USER_ID, USER_ID, MAIL);
		
		return getUserContractCase(USER_SUBSCRIBED_EVENT_NAME, event, invalidJson, SCHEMA_VERSION_2);
	}
	
	private static UserContractCase libraryCardAssignedCaseV1() {
		LibraryCardAssignedPayload payload = new LibraryCardAssignedPayload(USER_ID, "LIB-000001");
		LibraryCardAssignedEvent event = new LibraryCardAssignedEvent(USER_ID, 
				"07bf89ea-fd4e-4080-8fda-eb94679c4f6d", 0, payload, Instant.now());
		
		String invalidJson = """
	            {
	              "eventId": "07bf89ea-fd4e-4080-8fda-eb94679c4f6d",
	              "eventType": "USER_SUBSCRIBED",
	              "producer": "user-service",
	              "aggregateId": "%s",
	              "occurredAt": "2026-06-10T11:00:00Z",
	              "schemaVersion": 2,
	              "payload": {
	              	"id": "%s",
	                "mail": "%s"
	              }
	            }
	            """.formatted(USER_ID, USER_ID, MAIL);
		
		return getUserContractCase(LIBRARY_CARD_ASSIGNED_EVENT_NAME, event, invalidJson, SCHEMA_VERSION_1);
	}

	private static UserContractCase getUserContractCase(String eventName, UserEvent event, String invalidJson, int schemaVersion) {
		return new UserContractCase(eventName, SCHEMA_PATH.formatted(eventName, schemaVersion), SAMPLE_PATH.formatted(eventName, schemaVersion), invalidJson, event);
	}
	
	private void validateSchema(String eventSchema, UserEvent userEvent) {
		IntegrationEventEnvelope<?> integrationEvent = mapper.map(userEvent);
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
