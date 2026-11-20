package mentoring.acomi.assistantservice.messaging.notifications.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import mentoring.acomi.assistantservice.application.projection.ProjectionOperations;
import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.handlers.UserUpdatedV1NotificationHandler;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.UserUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@ExtendWith(MockitoExtension.class)
public class UserUpdatedV1NotificationHandlerTest  extends AbstractNotificationHandlerTest {

	private static final String IDENTITY_PROVIDER_ID = "ip1";

	private static final String EMAIL = "mario.rossi@test.it";

	private static final String USER_ID = "userId1";

	@Mock
	private ProjectionOperations<UserView> projection;
	
	private UserUpdatedV1NotificationHandler handler;
	
	@BeforeEach
	void setUp() {
		handler = new UserUpdatedV1NotificationHandler(projection, mapper);
	}
	
	@Override
	protected NotificationHandler handler() {
		return handler;
	}

	@Override
	protected NotificationEventEnvelope<UserUpdatedNotificationPayload> validEvent() {
		return getUserUpdatedEvent(USER_ID, EMAIL, IDENTITY_PROVIDER_ID, UserStatus.ACTIVE, UserRole.READER, 1);
	}
	
	@TestFactory
	Collection<DynamicTest> shouldRejectInvalidPayloads() {
		return invalidPayloads().stream().map(scenario -> DynamicTest.dynamicTest(scenario.description(), 
				     () -> assertInvalidPayload(scenario.event(), scenario.field(), projection))).toList();
	
	}
	
	private List<InvalidNotificationPayloadScenario> invalidPayloads() {
	    return List.of(new InvalidNotificationPayloadScenario("blank userId", "userId",
	    		         getUserUpdatedEvent("", EMAIL, IDENTITY_PROVIDER_ID, UserStatus.ACTIVE, UserRole.READER, 1)),
	    		       new InvalidNotificationPayloadScenario("blank email", "email",
	    		    	 getUserUpdatedEvent(USER_ID, "", IDENTITY_PROVIDER_ID, UserStatus.ACTIVE, UserRole.READER, 1)),
	    		       new InvalidNotificationPayloadScenario("blank userIdentityProviderId", "userIdentityProviderId",
	    		    	 getUserUpdatedEvent(USER_ID, EMAIL, "", UserStatus.ACTIVE, UserRole.READER, 1)),
	    		       new InvalidNotificationPayloadScenario("null status", "status",
	    		    	 getUserUpdatedEvent(USER_ID, EMAIL, IDENTITY_PROVIDER_ID, null, UserRole.READER, 1)),
	    		       new InvalidNotificationPayloadScenario("null role", "role",
	    		    	 getUserUpdatedEvent(USER_ID, EMAIL, IDENTITY_PROVIDER_ID, UserStatus.ACTIVE, null, 1)));
	}
	
	@Test
	void shouldHandleBookSubscriptionNotifiedEvent() {
		NotificationEventEnvelope<UserUpdatedNotificationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).upsert(eq(handler.getUser(event.payload())), any(Instant.class));
	}
			
	private NotificationEventEnvelope<UserUpdatedNotificationPayload> getUserUpdatedEvent(String userId, String email, 
			String userIdentityProviderId, UserStatus status, UserRole role, int schemaVersion) {
		
		return new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.BOOK_UPDATED, "test-handler", 
				Instant.now(), schemaVersion, getPayload(userId, email, userIdentityProviderId, status, role));
	}

	private UserUpdatedNotificationPayload getPayload(String userId, String email, String userIdentityProviderId, 
			UserStatus status, UserRole role) {
		return new UserUpdatedNotificationPayload(userId, email, "Mario", "Rossi", userIdentityProviderId, "CARD-0001", status, role);
	}
		
}
