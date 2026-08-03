package mentoring.acomi.loanservice.messaging.handler;

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

import mentoring.acomi.loanservice.application.projection.ProjectionDispatcher;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserUnsuspendedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@ExtendWith(MockitoExtension.class)
public class UserUnsuspendedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private ProjectionDispatcher dispatcher;
	
	private UserUnsuspendedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new UserUnsuspendedV1Handler(dispatcher, mapper);
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<UserIntegrationPayload> validEvent() {
		return getUserSuspendedEvent(UUID.randomUUID().toString(), UserStatus.ACTIVE, 1);
	}
	
	@Test
	void shouldHandleUserUnsuspendedEvent() {
		IntegrationEventEnvelope<UserIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isEmpty());
		verify(dispatcher).dispatch(event, event.payload());	
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), dispatcher))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank userId", "userId", getUserSuspendedEvent("", UserStatus.ACTIVE, 1)),
	                   new InvalidPayloadScenario("null status", "status", getUserSuspendedEvent(UUID.randomUUID().toString(), null, 1)));                  
	}
		
	private IntegrationEventEnvelope<UserIntegrationPayload> getUserSuspendedEvent(String userId, UserStatus status, int schemaVersion) {

		String aggregateId = userId == null || userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_UNSUSPENDED,
			"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, new UserIntegrationPayload(userId, status));
	}

}
