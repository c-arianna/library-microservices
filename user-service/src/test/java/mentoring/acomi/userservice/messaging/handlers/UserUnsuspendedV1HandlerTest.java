package mentoring.acomi.userservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserUnsuspendedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;

@ExtendWith(MockitoExtension.class)
public class UserUnsuspendedV1HandlerTest extends AbstractUserNotificationHandlerTest {
	
	private static final String USER_ID = UUID.randomUUID().toString();
	
	@Mock
	private UserProjection projection;
	private UserUnsuspendedV1Handler handler;
	
	@BeforeEach
	void setup() {
		handler = new UserUnsuspendedV1Handler(projection, mapper, notificationService);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventTypes eventType() {
		return handler.eventType();
	}

	@Override
	protected IntegrationEventEnvelope<UserIntegrationPayload> validEvent() {
		return getUserUnsuspendedEvent(USER_ID, UserStatus.ACTIVE, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED,
				"user-service", UUID.randomUUID().toString(), AggregateType.USER.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getUserUnsuspendedEvent(USER_ID, UserStatus.ACTIVE, schemaVersion);
	}

	@Override
	protected String expectedUserId() {
		return USER_ID;
	}
	@Test
	void shouldHandleUserUnsuspendedEvent() {
		IntegrationEventEnvelope<UserIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).unsuspendUser(event.payload(), event.occurredAt());
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank userId", "userId", getUserUnsuspendedEvent("", UserStatus.ACTIVE, 1)),
	    		       new InvalidPayloadScenario("null sfatus", "status", getUserUnsuspendedEvent(USER_ID, null,  1)));
	}
				
	private IntegrationEventEnvelope<UserIntegrationPayload> getUserUnsuspendedEvent(String userId, UserStatus status, int schemaVersion){
		
		String aggregateId = userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_UNSUSPENDED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				new UserIntegrationPayload(userId, status));
	}
}
