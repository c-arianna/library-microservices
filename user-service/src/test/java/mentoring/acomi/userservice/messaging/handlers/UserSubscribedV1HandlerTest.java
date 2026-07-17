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
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.projection.UserProjectionOperations;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserSubscribedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;

@ExtendWith(MockitoExtension.class)
public class UserSubscribedV1HandlerTest extends AbstractEventHandlerTest {
	
	private static final String IDENTITY_PROVIDER = "user123456";
	private static final String NAME = "Harry";
	private static final String LASTNAME = "Potter";
	private static final String EMAIL = "h.potter@gmail.com";

	@Mock
	private UserProjectionOperations projectionOperations;	
	private UserSubscribedV1Handler handler;
	
	@BeforeEach
	void setup() {
		handler = new UserSubscribedV1Handler(projectionOperations, mapper);
	}
	
	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<UserSubscribedIntegrationPayload> validEvent() {
		return getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, LASTNAME, IDENTITY_PROVIDER, UserStatus.ACTIVE, UserRole.READER, 1);
	}

	@Test
	void shouldHandleUserSubscribedEvent() {
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isPresent());
		verify(projectionOperations, times(1)).subscribeUser(event.payload(), event.occurredAt());
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projectionOperations))).toList();
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank userId", "userId", 
	    		           getUserSubscribedEvent("", EMAIL, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null email", "email", 
	                	   getUserSubscribedEvent(UUID.randomUUID().toString(), null, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, 
	                			   UserRole.READER, 1)),
	                   new InvalidPayloadScenario("invalid email", "email", 
	                	   getUserSubscribedEvent(UUID.randomUUID().toString(), "test1", NAME, LASTNAME, UUID.randomUUID().toString(), 
	                			   UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank name", "name", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, "", LASTNAME, UUID.randomUUID().toString(), 
								        UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank lastname", "lastname", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, "", UUID.randomUUID().toString(), 
	                    		        UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank userIdentityProviderId", "userIdentityProviderId", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, LASTNAME, "", UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null status", "status", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, LASTNAME, 
	                    		  		UUID.randomUUID().toString(), null, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null role", "role", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, LASTNAME, UUID.randomUUID().toString(),
	                    		        UserStatus.ACTIVE, null, 1)));
	                   
	}
								
	private IntegrationEventEnvelope<UserSubscribedIntegrationPayload> getUserSubscribedEvent(String userId, String email, String name, 
			String lastname, String userIdentityProviderId, UserStatus status, UserRole role, int schemaVersion){
		
		String aggregateId = userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				new UserSubscribedIntegrationPayload(userId, email, name, lastname, userIdentityProviderId, status, role));
	}
}
