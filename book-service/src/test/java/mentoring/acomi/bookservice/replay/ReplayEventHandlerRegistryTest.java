package mentoring.acomi.bookservice.replay;

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

import mentoring.acomi.bookservice.infrastructure.messaging.replay.ReplayBookHandlerFactory;
import mentoring.acomi.bookservice.infrastructure.messaging.replay.ReplayEventHandlerRegistry;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.NoOpEventHandler;

@ExtendWith(MockitoExtension.class)
public class ReplayEventHandlerRegistryTest {

	@Mock
	private ReplayBookHandlerFactory factory;
	
	private ReplayEventHandlerRegistry registry;
	
	@BeforeEach
	void setUp() {
		when(factory.createHandlers()).thenReturn(List.of(new ReplayableBookHandler()));
		registry = new ReplayEventHandlerRegistry(factory);
	}
    @Test
    void shouldFindReplayableHandler() {
        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.BOOK_REGISTERED, 1));
        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void shouldNotFindUnsupportedVersion() {
        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.BOOK_REGISTERED, 999));
        Assertions.assertTrue(result.isEmpty());
    }

    @HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REGISTERED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
    static class ReplayableBookHandler extends NoOpEventHandler {}

    private IntegrationEventEnvelope<?> event(IntegrationEventTypes type, int version) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), type, "test", UUID.randomUUID().toString(), "BOOK",
                0, Instant.now(), version, null);
    }
}