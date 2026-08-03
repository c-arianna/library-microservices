package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserSubscribedHandler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@ExtendWith(MockitoExtension.class)
public class UserSubscribedHandlerTest extends AbstractEventHandlerTest {

	private static final String MAIL = "test@gmail.com";
	private static final String NAME = "Test";
	private static final String LASTNAME = "Test";
	
	@Mock
	private ProjectionDispatcher dispatcher;

	private UserSubscribedHandler handler;

	@BeforeEach
	void setUp() {
		handler = new UserSubscribedHandler(dispatcher, mapper);
	}

	@Override
	protected EventHandler handler() {
		return handler;
	}

	@Override
	protected IntegrationEventEnvelope<UserSubscribedIntegrationPayload> validEvent() {
		return getUserSubscribedV1Event(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, 
				UserRole.READER, 1);
	}
	
	@Test
	void shouldHandleUserSubscribedEvent() {
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = validEvent();
		Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);
		Assertions.assertTrue(notification.isEmpty());
		verify(dispatcher).dispatch(event, event.payload());	
	}

	@Test
	void shouldHandleVersion2Event() {

	    IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event =
	            getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, UUID.randomUUID().toString(), "LIB-000001", 
	            		UserStatus.ACTIVE, UserRole.READER,  2);

	    Optional<ProjectionUpdateNotification> notification = handler.handleEvent(event);

	    Assertions.assertTrue(notification.isEmpty());

	    verify(dispatcher).dispatch(event, event.payload());	
	}
	
	@Test
	void shouldRejectVersion2WithoutCardNumberForReaderUser() {

	    IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event =
	    		 getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, UUID.randomUUID().toString(), null, 
		            		UserStatus.ACTIVE, UserRole.READER,  2);

	    Assertions.assertThrows(IllegalStateException.class, () -> handler.handleEvent(event));

	    verifyNoInteractions(dispatcher);
	}
	
	@Test
	void shouldRejectVersion2WithCardNumberForAdminUser() {

	    IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event =
	    		 getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, UUID.randomUUID().toString(), 
	    				 "LIB-000001", UserStatus.ACTIVE, UserRole.ADMIN,  2);

	    Assertions.assertThrows(IllegalStateException.class, () -> handler.handleEvent(event));

	    verifyNoInteractions(dispatcher);
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), dispatcher))).toList();
	
	}
	
	private List<InvalidPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidPayloadScenario("blank userId", "userId", 
	    		           getUserSubscribedV1Event("", MAIL, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null email", "email", 
	                	   getUserSubscribedV1Event(UUID.randomUUID().toString(), null, NAME, LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, 
	                			   UserRole.READER, 1)),
	                   new InvalidPayloadScenario("invalid email", "email", 
	                	   getUserSubscribedV1Event(UUID.randomUUID().toString(), "test1", NAME, LASTNAME, UUID.randomUUID().toString(), 
	                			   UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank name", "name", 
	                		   getUserSubscribedV1Event(UUID.randomUUID().toString(), MAIL, "", LASTNAME, UUID.randomUUID().toString(), 
								        UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank lastname", "lastname", 
	                		   getUserSubscribedV1Event(UUID.randomUUID().toString(), MAIL, NAME, "", UUID.randomUUID().toString(), 
	                    		        UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("blank userIdentityProviderId", "userIdentityProviderId", 
	                		   getUserSubscribedV1Event(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, "", UserStatus.ACTIVE, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null status", "status", 
	                		   getUserSubscribedV1Event(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, 
	                    		  		UUID.randomUUID().toString(), null, UserRole.READER, 1)),
	                   new InvalidPayloadScenario("null role", "role", 
	                		   getUserSubscribedV1Event(UUID.randomUUID().toString(), MAIL, NAME, LASTNAME, UUID.randomUUID().toString(),
	                    		        UserStatus.ACTIVE, null, 1)));
	                   
	}
								
	private IntegrationEventEnvelope<UserSubscribedIntegrationPayload> getUserSubscribedV1Event(String userId, String mail, String name, 
			String lastname, String identityProvider, UserStatus status, UserRole role, int schemaVersion) {
		return getUserSubscribedEvent(userId, mail, name, lastname, identityProvider, null, status, role, schemaVersion);
	}

	private IntegrationEventEnvelope<UserSubscribedIntegrationPayload> getUserSubscribedEvent(String userId, String mail, String name, 
			String lastname, String identityProvider, String cardNumber, UserStatus status, UserRole role, int schemaVersion) {

		String aggregateId = userId == null || userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				getPayload(userId, mail, name, lastname, identityProvider, cardNumber, status, role));
	}

	
	private UserSubscribedIntegrationPayload getPayload(String userId, String mail, String name, String lastname, 
			String identityProvider, String cardNumber, UserStatus status, UserRole role) {
		return new UserSubscribedIntegrationPayload(userId, mail, name, lastname, identityProvider, cardNumber, status, role);
	}

}
