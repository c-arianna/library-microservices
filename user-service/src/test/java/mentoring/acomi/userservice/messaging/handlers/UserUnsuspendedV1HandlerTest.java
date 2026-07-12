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
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.infrastructure.messaging.handlers.UserUnsuspendedV1Handler;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.UserNotificationService;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class UserUnsuspendedV1HandlerTest extends AbstractEventHandlerTest {
	
	@Mock
	private UserProjection projection;
	
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	private EventPayloadMapper mapper;
	
	@Mock
	private UserNotificationService notificationService;
	
	private UserUnsuspendedV1Handler handler;
	
	@BeforeEach
	void setup() {
		mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
		handler = new UserUnsuspendedV1Handler(projection, mapper, notificationService);
	}

	@Test
	void shouldHandleUserUnsuspendedEvent() {
		IntegrationEventEnvelope<UserIntegrationPayload> event = validEvent();
		handler.handleEvent(event);
		verify(projection, times(1)).unsuspendUser(event.payload(), event.occurredAt());
	}
	
	@Test
	void shouldRejectPayloadWithBlankUserId() {
		
		IntegrationEventEnvelope<UserIntegrationPayload> event = getUserUnsuspendedEvent("", UserStatus.ACTIVE, 1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("userId"),
	                    exception.getInvalidFields()),
	            () -> verifyNoInteractions(projection)
	    );
	}
	
		
	@Test
	void shouldRejectPayloadWithoutStatus() {
		
		IntegrationEventEnvelope<UserIntegrationPayload> event = getUserUnsuspendedEvent(UUID.randomUUID().toString(), null,  1);
		
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler.handleEvent(event));

		Assertions.assertAll(
	            () -> Assertions.assertEquals(
	                    Set.of("status"),
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
	protected IntegrationEventEnvelope<UserIntegrationPayload> validEvent() {
		return getUserUnsuspendedEvent(UUID.randomUUID().toString(), UserStatus.ACTIVE, 1);
	}

	@Override
	protected IntegrationEventEnvelope<?> differentEvent() {
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_SUBSCRIBED,
				"user-service", UUID.randomUUID().toString(), AggregateType.USER.name(), 1, Instant.now(), 1, Map.of());
	}

	@Override
	protected IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion) {
		return getUserUnsuspendedEvent(UUID.randomUUID().toString(), UserStatus.ACTIVE, schemaVersion);
	}

	private IntegrationEventEnvelope<UserIntegrationPayload> getUserUnsuspendedEvent(String userId, UserStatus status, int schemaVersion){
		
		String aggregateId = userId.isBlank() ? UUID.randomUUID().toString() : userId;
		
		return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), IntegrationEventTypes.USER_UNSUSPENDED,
				"test-handler", aggregateId, AggregateType.USER.name(), 0, Instant.now(), schemaVersion, 
				new UserIntegrationPayload(userId, status));
	}
}
