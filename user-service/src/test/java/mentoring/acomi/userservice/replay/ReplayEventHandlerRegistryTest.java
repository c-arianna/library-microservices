package mentoring.acomi.userservice.replay;

import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.NoOpEventHandler;
import mentoring.acomi.userservice.infrastructure.messaging.replay.ReplayEventHandlerRegistry;
import mentoring.acomi.userservice.infrastructure.messaging.replay.ReplayUserHandlerFactory;

@ExtendWith(MockitoExtension.class)
public class ReplayEventHandlerRegistryTest {

	@Mock
	private ReplayUserHandlerFactory factory;
	
	private ReplayEventHandlerRegistry registry;
	
	@BeforeEach
	void setUp() {
		when(factory.createHandlers()).thenReturn(List.of(new ReplayableUserHandler()));
		registry = new ReplayEventHandlerRegistry(factory);
	}
    @Test
    void shouldFindReplayableHandler() {
        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.USER_UNSUSPENDED, 1));
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void shouldNotFindUnsupportedVersion() {
        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.USER_UNSUSPENDED, 999));
        Assertions.assertTrue(result.isEmpty());
    }

    @HandlerMetadata(eventType = IntegrationEventTypes.USER_UNSUSPENDED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
    static class ReplayableUserHandler extends NoOpEventHandler {}

    private IntegrationEventEnvelope<?> event(IntegrationEventTypes type, int version) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), type, "test", UUID.randomUUID().toString(), "LOAN",
                0, Instant.now(), version, null);
    }
}
