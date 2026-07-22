package mentoring.acomi.loanservice.messaging.handler;

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

import mentoring.acomi.loanservice.application.projection.UserProjectionOperations;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.LibraryCardAssignedHandler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.LibraryCardAssignedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

@ExtendWith(MockitoExtension.class)
public class LibraryCardAssignedHandlerTest extends AbstractEventHandlerTest {
	
	@Mock
	private UserProjectionOperations projectionOperations;
	private LibraryCardAssignedHandler handler;

	@BeforeEach
	void setUp() {
		handler = new LibraryCardAssignedHandler(projectionOperations, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<LibraryCardAssignedIntegrationPayload> validEvent() {
		return getLibraryCardAssignedEvent(UUID.randomUUID().toString(), "LIB-000001", 1);
	}
	
	@Test
	void shouldHandleLibraryCardAssignedEvent() {
		IntegrationEventEnvelope<LibraryCardAssignedIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isEmpty());
		verify(projectionOperations, times(1)).assignCardNumber(event.payload().userId(), event.payload().cardNumber(),
				event.occurredAt());
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank userId", "userId", getLibraryCardAssignedEvent("", "LIB-000001", 1)),
	                   new InvalidPayloadScenario("blank cardNumber", "cardNumber", 
	                	   getLibraryCardAssignedEvent(UUID.randomUUID().toString(), "", 1)));
	                   
	}
								
	private IntegrationEventEnvelope<LibraryCardAssignedIntegrationPayload> getLibraryCardAssignedEvent(String userId, 
			String cardNumber, int schemaVersion) {

		String aggregateId = userId == null || userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.LIBRARY_CARD_ASSIGNED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				new LibraryCardAssignedIntegrationPayload(userId, cardNumber));
	}

}