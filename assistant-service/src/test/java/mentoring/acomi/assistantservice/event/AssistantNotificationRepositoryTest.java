package mentoring.acomi.assistantservice.event;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import mentoring.acomi.assistantservice.config.SecurityTestConfig;
import mentoring.acomi.assistantservice.infrastructure.messaging.notifications.payload.BookUpdatedNotificationPayload;
import mentoring.acomi.assistantservice.infrastructure.persistence.repositories.AssistantNotificationRepository;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@SpringBootTest
@Transactional
@Import(SecurityTestConfig.class)
@ActiveProfiles("test")
public class AssistantNotificationRepositoryTest {

	private static final int EVENT_VERSION = 1;
	
	@Autowired
	private AssistantNotificationRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Test
	public void shouldSaveNotificationEvent() {

		String isbn = "9788804336327";
		NotificationEventEnvelope<BookUpdatedNotificationPayload> bookUpdateNotificationEvent = createBookUpdateNotificationEvent(isbn);

		repository.save(bookUpdateNotificationEvent);

		entityManager.clear();

		Assertions.assertTrue(repository.exists(bookUpdateNotificationEvent.eventId()));
	}

	@Test
	public void shouldReturnFalseIfEventDoesNotExist() {
		boolean exists = repository.exists("event1");
		Assertions.assertFalse(exists);
	}

	private NotificationEventEnvelope<BookUpdatedNotificationPayload> createBookUpdateNotificationEvent(String isbn) {
		String eventId = UUID.randomUUID().toString();

		BookUpdatedNotificationPayload payload = new BookUpdatedNotificationPayload(isbn, "Italo Calvino", "Il barone rampante", "",
				false, 0, 0, 0);
		return new NotificationEventEnvelope<>(eventId, NotificationEventType.BOOK_UPDATED, "assistant-test", Instant.now(), 
				EVENT_VERSION, payload);

	}

}