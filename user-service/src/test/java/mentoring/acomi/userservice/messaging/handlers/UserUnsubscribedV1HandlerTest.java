package mentoring.acomi.userservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.projection.UserProjectionOperations;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserUnsubscribedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;

@ExtendWith(MockitoExtension.class)
public class UserUnsubscribedV1HandlerTest extends AbstractEventHandlerTest {
	
	@Mock
	private UserProjectionOperations projectionOperations;
	private UserUnsubscribedV1Handler handler;
	
	@BeforeEach
	void setup() {
		handler = new UserUnsubscribedV1Handler(projectionOperations, mapper);
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}
	
	@Override
	protected IntegrationEventEnvelope<UserIntegrationPayload> validEvent() {
		return getUserUnsubscribedEvent(UUID.randomUUID().toString(), UserStatus.DISABLED, 1);
	}
	
	@Test
	void shouldHandleUserUnsubscribedEvent() {
		IntegrationEventEnvelope<UserIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		verify(projectionOperations, times(1)).unsubscribeUser(event.payload().userId(), event.occurredAt());
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank userId", "userId", getUserUnsubscribedEvent("", UserStatus.DISABLED, 1)),
	    		       new InvalidPayloadScenario("null status", "status", getUserUnsubscribedEvent(UUID.randomUUID().toString(), null,  1)));
	}
				
	private IntegrationEventEnvelope<UserIntegrationPayload> getUserUnsubscribedEvent(String userId, UserStatus status, int schemaVersion){
		
		String aggregateId = userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_UNSUBSCRIBED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				new UserIntegrationPayload(userId, status));
	}
}
