package mentoring.acomi.notificationservice.messaging.handler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import mentoring.acomi.notificationservice.application.errors.NotificationHandlingException;
import mentoring.acomi.notificationservice.infrastructure.messaging.BookRegisteredV1NotificationHandler;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookRegisteredNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BookRegisteredNotificationHandlerTest {

	private static final String BOOK_REGISTERED_EVENT_NAME = IntegrationEventTypes.BOOK_REGISTERED.eventName;

	private static final String BOOK_SAMPLE_PATH = "contracts/book/%s/v1/sample.json";
	private static final String BOOK_SCHEMA_PATH = "contracts/book/%s/v1/event.schema.json";

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	private ObjectMapper mapper = new ObjectMapper();

	private BookRegisteredV1NotificationHandler handler;

	@BeforeEach
	void setUp() {
		handler = new BookRegisteredV1NotificationHandler(mapper, messagingTemplate);
	}

	@Test
	void shouldSendNotification() {

		JsonNode eventJson = buildBookRegisteredEvent();

		IntegrationEventEnvelope<?> event = mapper.treeToValue(eventJson, IntegrationEventEnvelope.class);
		handler.handleEvent(event);

		ArgumentCaptor<EventNotification> eventCaptor = ArgumentCaptor.forClass(EventNotification.class);

		verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/books"), eventCaptor.capture());
		verifyNoMoreInteractions(messagingTemplate);

		EventNotification sentEvent = eventCaptor.getValue();

		Assertions.assertEquals(IntegrationEventTypes.BOOK_REGISTERED, sentEvent.eventType());

		BookRegisteredNotificationPayload payload = (BookRegisteredNotificationPayload) sentEvent.payload();

		JsonNode expectedPayload = eventJson.path("payload");

		Assertions.assertAll(() -> Assertions.assertEquals(expectedPayload.path("isbn").asString(), payload.isbn()),
				() -> Assertions.assertEquals(expectedPayload.path("author").asString(), payload.author()),
				() -> Assertions.assertEquals(expectedPayload.path("title").asString(), payload.title()),
				() -> Assertions.assertEquals(expectedPayload.path("description").asString(), payload.description()));

	}

	@Test
	void shouldSupportVersion1() {

		IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
				IntegrationEventTypes.BOOK_REGISTERED, "book-service", "9788804336327", "BOOK", 1, Instant.now(), 1,
				Map.of());

		Assertions.assertTrue(handler.accepts(event));
	}

	@Test
	void shouldNotSupportUnknownSchemaVersion() {

		IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
				IntegrationEventTypes.BOOK_REGISTERED, "book-service", "9788804336327", "BOOK", 1, Instant.now(), 999,
				Map.of());

		Assertions.assertFalse(handler.accepts(event));
	}

	@Test
	void shouldNotSupportDifferentEventType() {

		IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
				IntegrationEventTypes.BOOK_BORROWED, "book-service", "9788804336327", "BOOK", 1, Instant.now(), 1,
				Map.of());

		Assertions.assertFalse(handler.accepts(event));
	}

	@Test
	void shouldRejectNullPayload() {

		IntegrationEventEnvelope<?> event = new IntegrationEventEnvelope<>(UUID.randomUUID().toString(),
				IntegrationEventTypes.BOOK_REGISTERED, "book-service", "9788804336327", "BOOK", 0, Instant.now(), 1,
				null);

		Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));

		verifyNoInteractions(messagingTemplate);
	}

	private JsonNode buildBookRegisteredEvent() {

		JsonNode eventJson = mapper.readTree(loadResource(BOOK_SAMPLE_PATH.formatted(BOOK_REGISTERED_EVENT_NAME)));
		Schema schema = loadSchema(BOOK_SCHEMA_PATH.formatted(BOOK_REGISTERED_EVENT_NAME));

		List<Error> validationErrors = schema.validate(eventJson);
		Assertions.assertTrue(validationErrors.isEmpty(), validationErrors.toString());

		return eventJson;
	}

	private InputStream loadResource(String path) {

		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

		if (is == null) {
			throw new IllegalStateException(String.format("File not found in classpath: %s", path));
		}

		return is;
	}

	private Schema loadSchema(String path) {
		SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
		return registry.getSchema(loadResource(path));
	}

}
