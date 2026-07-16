package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserSubscribedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@ExtendWith(MockitoExtension.class)
public class UserSubscribedV1HandlerTest extends AbstractEventHandlerTest {

	private static final String MAIL = "test@gmail.com";
	private static final String NAME = "Test";
	private static final String LASTNAME = "Test";
	
	@Mock
	private UserProjection projection;
	private UserSubscribedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new UserSubscribedV1Handler(projection, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<UserSubscribedIntegrationPayload> validEvent() {
		return getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, 
				UserRole.READER, 1);
	}
	
	@Test
	void shouldHandleUserSubscribedEvent() {
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).handleSubscribeUser(event.payload(), event.occurredAt());
	}

	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank userId", "userId", 
	    		           getUserSubscribedEvent("", MAIL, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null email", "email", 
	                	   getUserSubscribedEvent(UUID.randomUUID().toString(), null, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, 
	                			   UserRole.READER, 1)),
	                   new InvalidPayloadScenario("invalid email", "email", 
	                	   getUserSubscribedEvent(UUID.randomUUID().toString(), "test1", NAME, LASTNAME, UUID.randomUUID().toString(), 
	                			   UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank name", "name", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, "", LASTNAME, UUID.randomUUID().toString(), 
								        UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank lastname", "lastname", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, "", UUID.randomUUID().toString(), 
	                    		        UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank userIdentityProviderId", "userIdentityProviderId", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, "", UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null status", "status", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, 
	                    		  		UUID.randomUUID().toString(), null, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null role", "role", 
	                		   getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, UUID.randomUUID().toString(),
	                    		        UserStatus.ACTIVE, null, 1)));
	                   
	}
								
	private IntegrationEventEnvelope<UserSubscribedIntegrationPayload> getUserSubscribedEvent(String userId, String mail, String name, 
			String lastname, String identityProvider, UserStatus status, UserRole role, int schemaVersion) {

		String aggregateId = userId == null || userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				getPayload(userId, mail, name, lastname, identityProvider, status, role));
	}

	private UserSubscribedIntegrationPayload getPayload(String userId, String mail, String name, String lastname, String identityProvider,
			UserStatus status, UserRole role) {
		return new UserSubscribedIntegrationPayload(userId, mail, name, lastname, identityProvider, status, role);
	}

}
