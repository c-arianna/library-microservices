package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcodelibrary.event.handlers.InvalidEventPayloadException;
import mentoring.acomi.sharedcorelibrary.integration.messaging.EventHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.ObjectMapper;

public abstract class AbstractEventHandlerTest {

	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    protected final EventPayloadMapper mapper = new EventPayloadMapper(OBJECT_MAPPER, VALIDATOR);
    
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
    
    protected void assertInvalidPayload(IntegrationEventEnvelope<?> event, String field, Object... mocks) {
		InvalidEventPayloadException exception = Assertions.assertThrows(InvalidEventPayloadException.class, () -> handler().handleEvent(event));
		Assertions.assertEquals(Set.of(field), exception.getInvalidFields());
		verifyNoInteractions(mocks);
	}
}