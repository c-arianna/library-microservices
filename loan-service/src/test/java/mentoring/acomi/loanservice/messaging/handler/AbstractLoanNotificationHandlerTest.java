package mentoring.acomi.loanservice.messaging.handler;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import mentoring.acomi.loanservice.infrastructure.messaging.notifications.LoanNotificationService;

public abstract class AbstractLoanNotificationHandlerTest extends AbstractEventHandlerTest {

	@Mock
	protected LoanNotificationService notificationService;

	protected abstract String expectedLoanId();

	protected int expectedSchemaVersion() {
		return 1;
	}

	@Test
	void shouldPublishBookUpdatedNotification() {
		handler().handleEvent(validEvent());
		verify(notificationService).publishLoanUpdated(expectedLoanId(), expectedSchemaVersion());
	}
	
}
