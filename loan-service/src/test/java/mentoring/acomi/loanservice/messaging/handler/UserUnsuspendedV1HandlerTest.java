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
import mentoring.acomi.loanservice.infrastructure.messaging.handlers.UserUnsuspendedV1Handler;
import mentoring.acomi.loanservice.infrastructure.messaging.payload.consumer.UserIntegrationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserUnsuspendedV1HandlerTest extends AbstractEventHandlerTest {

	@Mock
	private UserProjection projection;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;

	private UserUnsuspendedV1Handler handler;

	@BeforeEach
	void setUp() {
        mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new UserUnsuspendedV1Handler(projection, mapper);
	}
	
	@Test
	void shouldHandleUserSubscribedEvent() {
	
		IntegrationEventEnvelope<UserIntegrationPayload> event = validEvent();
		
		handler.handleEvent(event);

		verify(projection, times(1)).handleUpdateUserStatus(event.payload(), event.occurredAt());
	}
	
	@Test
	void shouldRejectPayloadWithBlankUserId() {
		
		IntegrationEventEnvelope<UserIntegrationPayload> event = getUserSuspendedEvent("", UserStatus.ACTIVE, 1);
		
		Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(projection);
	}
	
	@Test
	void shouldRejectPayloadWithoutStatus() {
		
		IntegrationEventEnvelope<UserIntegrationPayload> event  = getUserSuspendedEvent(UUID.randomUUID().toString(), null, 1);
		
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
	protected IntegrationEventEnvelope<UserIntegrationPayload> validEvent() {
		return getUserSuspendedEvent(UUID.randomUUID().toString(), UserStatus.ACTIVE, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_UNSUBSCRIBED,
				"user-service", UUID.randomUUID().toString(), AggregateType.USER.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getUserSuspendedEvent(UUID.randomUUID().toString(), UserStatus.SUSPENDED, schemaVersion);
	}
	
	private IntegrationEventEnvelope<UserIntegrationPayload> getUserSuspendedEvent(String userId, UserStatus status, int schemaVersion) {

		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_UNSUSPENDED,
				"test-handler", UUID.randomUUID().toString(), AggregateType.USER.name(), 0, Instant.now(), schemaVersion,  
				new UserIntegrationPayload(userId, status));
	}

}
