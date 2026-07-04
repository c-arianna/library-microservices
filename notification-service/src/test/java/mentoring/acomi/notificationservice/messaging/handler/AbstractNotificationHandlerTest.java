package mentoring.acomi.notificationservice.messaging.handler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public class AbstractNotificationHandlerTest {

    private final TestNotificationHandler handler = new TestNotificationHandler();

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldThrowExceptionWhenFieldMissing() {

        ObjectNode payload = mapper.createObjectNode();

        NotificationHandlingException ex = Assertions.assertThrows(NotificationHandlingException.class,
                        () -> handler.requiredField(payload, "isbn"));

        Assertions.assertEquals("Missing required field 'isbn'", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFieldBlank() {

        ObjectNode payload = mapper.createObjectNode();

        payload.put("isbn", "");

        NotificationHandlingException ex = Assertions.assertThrows(NotificationHandlingException.class,
                        () -> handler.requiredField(payload, "isbn"));

        Assertions.assertEquals("Blank required field 'isbn'", ex.getMessage());
    }

    @Test
    void shouldReturnFieldValue() {

        ObjectNode payload = mapper.createObjectNode();

        payload.put("isbn", "9788804336327");

        Assertions.assertEquals("9788804336327", handler.optionalField(payload, "isbn"));
    }
    
    @Test
	void shouldRejectNonStringField() {

    	ObjectNode payload = mapper.createObjectNode();

    	payload.putArray("isbn");

        NotificationHandlingException ex = Assertions.assertThrows(NotificationHandlingException.class,
                        () -> handler.requiredField(payload, "isbn"));

        Assertions.assertEquals("Field 'isbn' must be a string", ex.getMessage());

	}
}
