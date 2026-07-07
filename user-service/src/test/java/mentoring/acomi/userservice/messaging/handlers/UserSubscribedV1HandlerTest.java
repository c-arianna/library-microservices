package mentoring.acomi.userservice.messaging.handlers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcodelibrary.event.handlers.InvalidEventPayloadException;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserSubscribedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserSubscribedV1HandlerTest extends AbstractEventHandlerTest {
	
	private static final String IDENTITY_PROVIDER = "user123456";
	private static final String NAME = "Harry";
	private static final String LASTNAME = "Potter";
	private static final String EMAIL = "h.potter@gmail.com";

	@Mock
	private UserProjection projection;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;
	
	private UserSubscribedV1Handler handler;
	
	@BeforeEach
	void setup() {
		mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new UserSubscribedV1Handler(projection, mapper);
	}

	@Test
	void shouldHandleUserSubscribedEvent() {
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).subscribeUser(event.payload(), event.occurredAt());
	}
	
	@Test
	void shouldRejectPayloadWithBlankUserId() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent("", EMAIL, NAME, LASTNAME, IDENTITY_PROVIDER, 
				UserStatus.ACTIVE, UserRole.READER, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("userId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithoutEmail() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), null, 
				NAME, LASTNAME, IDENTITY_PROVIDER, UserStatus.ACTIVE, UserRole.READER, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("email"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithInvalidEmail() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), "test", 
				NAME, LASTNAME, IDENTITY_PROVIDER, UserStatus.ACTIVE, UserRole.READER, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("email"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithBlankName() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, "", 
				LASTNAME, IDENTITY_PROVIDER, UserStatus.ACTIVE, UserRole.READER, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("name"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithBlankLastname() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, "", 
				IDENTITY_PROVIDER, UserStatus.ACTIVE, UserRole.READER, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("lastname"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithBlankIdentityProvider() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, 
				NAME, LASTNAME, "", UserStatus.ACTIVE, UserRole.READER, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("userIdentityProviderId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithoutStatus() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, 
				LASTNAME, IDENTITY_PROVIDER, null, UserRole.READER, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("status"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
	@Test
	void shouldRejectPayloadWithoutRole() {
		
		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, 
				LASTNAME, IDENTITY_PROVIDER, UserStatus.ACTIVE, null, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("role"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
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
	protected IntegrationEventEnvelope<UserSubscribedIntegrationPayload> validEvent() {
		return getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, LASTNAME, IDENTITY_PROVIDER, UserStatus.ACTIVE, 
				UserRole.READER, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUSPENDED,
				"user-service", UUID.randomUUID().toString(), AggregateType.USER.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getUserSubscribedEvent(UUID.randomUUID().toString(), EMAIL, NAME, LASTNAME, IDENTITY_PROVIDER, UserStatus.ACTIVE, 
				UserRole.READER, schemaVersion);
	}

	private IntegrationEventEnvelope<UserSubscribedIntegrationPayload> getUserSubscribedEvent(String userId, String email, String name, 
			String lastname, String userIdentityProviderId, UserStatus status, UserRole role, int schemaVersion){
		
		String aggregateId = userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				new UserSubscribedIntegrationPayload(userId, email, name, lastname, userIdentityProviderId, status, role));
	}
}
