package mentoring.acomi.notificationservice.messaging.handler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerMetadata;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandlerRegistry;

public class NotificationHandlerRegistryTest {

	@Test
    void shouldFindHandlerForSupportedVersion() {

		NotificationHandlerRegistry registry = new NotificationHandlerRegistry(List.of(new Handler1()));

        Optional<NotificationHandler> handler = registry.find(event(NotificationEventType.BOOK_UPDATED, 1));

        Assertions.assertTrue(handler.isPresent());
        Assertions.assertInstanceOf(Handler1.class, handler.get());
        
    }

    @Test
    void shouldNotFindHandlerForUnsupportedVersion() {

    	NotificationHandlerRegistry registry = new NotificationHandlerRegistry(List.of(new Handler1()));

        Optional<NotificationHandler> handler = registry.find(event(NotificationEventType.BOOK_UPDATED, 999));

        Assertions.assertTrue(handler.isEmpty());
    }

    @Test
    void shouldNotFindHandlerForDifferentEventType() {

    	NotificationHandlerRegistry registry = new NotificationHandlerRegistry(List.of(new Handler1()));

        Optional<NotificationHandler> handler = registry.find(event(NotificationEventType.LOAN_UPDATED, 1));

        Assertions.assertTrue(handler.isEmpty());
    }

    @Test
    void shouldFailWhenAnnotationIsMissing() {

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, 
        		     () -> new NotificationHandlerRegistry(List.of(new MissingAnnotationHandler())));

        Assertions.assertTrue(exception.getMessage().contains("Missing"));
    }

    @Test
    void shouldFailWhenDuplicateHandlerExists() {

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                                  () -> new NotificationHandlerRegistry(List.of(new Handler1(), new DuplicateHandler1())));

        Assertions.assertTrue(exception.getMessage().contains("Duplicate handler"));
    }

    private NotificationEventEnvelope<?> event(NotificationEventType eventType, int schemaVersion) {

        return new NotificationEventEnvelope<>(UUID.randomUUID().toString(), eventType, "test", Instant.now(), schemaVersion, 
        		Map.of());
    }

    @NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_UPDATED, supportedVersions = {1})
    static class Handler1 implements NotificationHandler {

        @Override
        public void handleEvent(NotificationEventEnvelope<?> event) { }
    }

    @NotificationHandlerMetadata(notificationEventType = NotificationEventType.BOOK_UPDATED, supportedVersions = {1})
    static class DuplicateHandler1 implements NotificationHandler {

        @Override
        public void handleEvent(NotificationEventEnvelope<?> event) { }
    }

    static class MissingAnnotationHandler implements NotificationHandler {

        @Override
        public void handleEvent(NotificationEventEnvelope<?> event) { }
    }
}
