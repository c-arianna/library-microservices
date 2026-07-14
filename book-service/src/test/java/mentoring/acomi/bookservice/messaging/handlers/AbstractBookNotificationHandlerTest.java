package mentoring.acomi.bookservice.messaging.handlers;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import mentoring.acomi.bookservice.infrastructure.messaging.notifications.BookNotificationService;

public abstract class AbstractBookNotificationHandlerTest extends AbstractEventHandlerTest {

	@Mock
	protected BookNotificationService notificationService;

	protected abstract String expectedIsbn();

	protected int expectedSchemaVersion() {
		return 1;
	}

	@Test
	void shouldPublishBookUpdatedNotification() {
		handler().handleEvent(validEvent());
		verify(notificationService).publishBookUpdated(expectedIsbn(), expectedSchemaVersion());
	}
	
}
