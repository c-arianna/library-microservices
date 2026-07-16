package mentoring.acomi.sharedcorelibrary.integration.messaging;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EventHandlerRegistryTest {

    @Test
    void shouldFindHandlerForSupportedVersion() {

        EventHandlerRegistry registry = new EventHandlerRegistry(List.of(new BookBorrowedV1Handler()));

        Optional<EventHandler> handler = registry.find(event(IntegrationEventTypes.BOOK_BORROWED, 1));

        Assertions.assertTrue(handler.isPresent());
        Assertions.assertInstanceOf(BookBorrowedV1Handler.class, handler.get());
        
    }

    @Test
    void shouldNotFindHandlerForUnsupportedVersion() {

        EventHandlerRegistry registry = new EventHandlerRegistry(List.of(new BookBorrowedV1Handler()));

        Optional<EventHandler> handler = registry.find(event(IntegrationEventTypes.BOOK_BORROWED, 999));

        Assertions.assertTrue(handler.isEmpty());
    }

    @Test
    void shouldNotFindHandlerForDifferentEventType() {

        EventHandlerRegistry registry = new EventHandlerRegistry(List.of(new BookBorrowedV1Handler()));

        Optional<EventHandler> handler = registry.find(event(IntegrationEventTypes.BOOK_RETURNED, 1));

        Assertions.assertTrue(handler.isEmpty());
    }

    @Test
    void shouldFailWhenAnnotationIsMissing() {

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, 
        		     () -> new EventHandlerRegistry(List.of(new MissingAnnotationHandler())));

        Assertions.assertTrue(exception.getMessage().contains("Missing"));
    }

    @Test
    void shouldFailWhenDuplicateHandlerExists() {

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                                  () -> new EventHandlerRegistry(List.of(new BookBorrowedV1Handler(), new DuplicateBookBorrowedV1Handler())));

        Assertions.assertTrue(exception.getMessage().contains("Duplicate handler"));
    }

    private IntegrationEventEnvelope<?> event(IntegrationEventTypes eventType, int schemaVersion) {

        return new IntegrationEventEnvelope<>(UUID.randomUUID().toString(), eventType, "test", "BOOK", UUID.randomUUID().toString(),
                1, Instant.now(), schemaVersion, Map.of());
    }

    @HandlerMetadata(eventType = IntegrationEventTypes.BOOK_BORROWED, supportedVersions = {1})
    static class BookBorrowedV1Handler implements EventHandler {

        @Override
        public Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> event) {
        	return Optional.empty();
        }
    }

    @HandlerMetadata(eventType = IntegrationEventTypes.BOOK_BORROWED, supportedVersions = {1}
    )
    static class DuplicateBookBorrowedV1Handler implements EventHandler {

        @Override
        public Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> event) {
        	return Optional.empty();
        }
    }

    static class MissingAnnotationHandler implements EventHandler {

        @Override
        public Optional<ProjectionUpdateNotification> handleEvent(IntegrationEventEnvelope<?> event) {
        	return Optional.empty();
        }
    }

}