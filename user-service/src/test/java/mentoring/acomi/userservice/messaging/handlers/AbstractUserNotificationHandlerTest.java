package mentoring.acomi.userservice.messaging.handlers;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import mentoring.acomi.userservice.infrastructure.messaging.notifications.UserNotificationService;

public abstract class AbstractUserNotificationHandlerTest extends AbstractEventHandlerTest {

	@Mock
	protected UserNotificationService notificationService;

	protected abstract String expectedUserId();

	protected int expectedSchemaVersion() {
		return 1;
	}

	@Test
	void shouldPublishBookUpdatedNotification() {
		handler().handleEvent(validEvent());
		verify(notificationService).publishUserUpdated(expectedUserId(), expectedSchemaVersion());
	}
	
}