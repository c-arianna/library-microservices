package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.InvalidEventPayloadException;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.loanservice.application.projection.UserProjection;
import mentoring.acomi.loanservice.domain.events.AggregateType;
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserSubscribedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserSubscribedIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserSubscribedV1HandlerTest extends AbstractEventHandlerTest {

	private static final String MAIL = "test@gmail.com";
	private static final String NAME = "Test";
	private static final String LASTNAME = "Test";
	
	@Mock
	private UserProjection projection;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;

	private UserSubscribedV1Handler handler;

	@BeforeEach
	void setUp() {
        mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new UserSubscribedV1Handler(projection, mapper);
	}

	@Test
	void shouldHandleUserSubscribedEvent() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = validEvent();

		handler.handleEvent(event);

		verify(projection, times(1)).handleSubscribeUser(event.payload(), event.occurredAt());

	}

	@Test
	void shouldRejectPayloadWithBlankUserId() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent("", MAIL, NAME, LASTNAME, 
				UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithoutEmail() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), null, NAME, 
				LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithInvalidEmail() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), "test1", NAME, LASTNAME, 
				UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithBlankName() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, "", LASTNAME, 
				UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithBlankLastname() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, "", 
				UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}

	@Test
	void shouldRejectPayloadWithBlankIdentityProvider() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME,
				LASTNAME, "", UserStatus.ACTIVE, UserRole.READER, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithoutStatus() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, 
				LASTNAME, UUID.randomUUID().toString(), null, UserRole.READER, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithoutRole() {

		IntegrationEventEnvelope<UserSubscribedIntegrationPayload> event = getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME, 
				LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, null, 1);

		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
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
		return getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME,
				LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUSPENDED,
				"user-service", UUID.randomUUID().toString(), AggregateType.USER.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getUserSubscribedEvent(UUID.randomUUID().toString(), MAIL, NAME,
				LASTNAME, UUID.randomUUID().toString(), UserStatus.ACTIVE, UserRole.READER, schemaVersion);
	}

	private IntegrationEventEnvelope<UserSubscribedIntegrationPayload> getUserSubscribedEvent(String userId, String mail, String name, 
			String lastname, String identityProvider, UserStatus status, UserRole role, int schemaVersion) {

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED,
				"test-handler", UUID.randomUUID().toString(), AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				getPayload(userId, mail, name, lastname, identityProvider, status, role));
	}

	private UserSubscribedIntegrationPayload getPayload(String userId, String mail, String name, String lastname, String identityProvider,
			UserStatus status, UserRole role) {
		return new UserSubscribedIntegrationPayload(userId, mail, name, lastname, identityProvider, status, role);
	}

}
