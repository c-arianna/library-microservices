package mentoring.acomi.sharedcorelibrary.replay;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.HandlerMode;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.integration.messaging.NoOpEventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.ProjectionUpdateNotification;

class ReplayEventHandlerRegistryTest {

    @Test
    void shouldFindHandler() {

        ReplayEventHandlerRegistry registry = new ReplayEventHandlerRegistry(List.of(new TestHandler()));

        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.BOOK_REGISTERED, 1));

        Assertions.assertTrue(result.isPresent());
    }

    @Test
    void shouldNotFindUnsupportedVersion() {

        ReplayEventHandlerRegistry registry = new ReplayEventHandlerRegistry(List.of(new TestHandler()));

        Optional<EventHandler> result = registry.find(event(IntegrationEventTypes.BOOK_REGISTERED, 999));

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldFailWhenMetadataIsMissing() {
    	Assertions.assertThrows(IllegalStateException.class, () -> new ReplayEventHandlerRegistry(List.of(new MissingMetadataHandler())));
    }
    
    @Test
    void shouldFailWhenDuplicateHandlerExists() {
    	Assertions.assertThrows(IllegalStateException.class, () -> new ReplayEventHandlerRegistry(List.of(new Handler1(), new Handler2())));
    }
    
    @HandlerMetadata(eventType = IntegrationEventTypes.BOOK_REGISTERED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
    static class TestHandler extends NoOpEventHandler {
    }

    @HandlerMetadata(eventType = IntegrationEventTypes.BOOK_BORROWED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
    static class Handler1 extends NoOpEventHandler {
    }

    @HandlerMetadata(eventType = IntegrationEventTypes.BOOK_BORROWED, supportedVersions = {1}, mode = HandlerMode.REPLAYABLE)
    static class Handler2 extends NoOpEventHandler {
    }

    
    static class MissingMetadataHandler implements EventHandler {

        @Override
        public Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> event) {
        	return Optional.empty();
        }
    }
    
    private IntegrationEventEnvelope<?> event(IntegrationEventTypes type, int version) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), type, "test", UUID.randomUUID().toString(), "TEST",  0,
                Instant.now(), version, Map.of());
    }
}