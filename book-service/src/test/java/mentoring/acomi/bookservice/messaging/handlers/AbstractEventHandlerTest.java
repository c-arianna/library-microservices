package mentoring.acomi.bookservice.messaging.handlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;

public abstract class AbstractEventHandlerTest {

    protected abstract EventHandler handler();

    protected abstract IntegrationEventTypes eventType();

    protected abstract IntegrationEventEnvelope<?> validEvent();

    protected abstract IntegrationEventEnvelope<?> differentEvent();

    protected abstract IntegrationEventEnvelope<?> withSchemaVersion(int schemaVersion);

    @Test
    void shouldSupportVersion1() {
        Assertions.assertTrue(handler().accepts(validEvent()));
    }

    @Test
    void shouldNotSupportUnknownSchemaVersion() {
        Assertions.assertFalse(handler().accepts(withSchemaVersion(999)));
    }

    @Test
    void shouldNotSupportDifferentEventType() {
        Assertions.assertFalse(handler().accepts(differentEvent()));
    }

    @Test
    void shouldReturnSupportedEventType() {
        Assertions.assertEquals(eventType(), handler().eventType());
    }
}