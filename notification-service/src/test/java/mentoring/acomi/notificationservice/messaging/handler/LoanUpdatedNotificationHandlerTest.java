package mentoring.acomi.notificationservice.messaging.handler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import mentoring.acomi.notificationservice.infrastructure.messaging.dto.EventNotification;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.LoanStatus;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.LoanUpdatedNotificationPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.LoanUpdatedV1NotificationHandler;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.error.NotificationHandlingException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LoanUpdatedNotificationHandlerTest {
	
	private static final String CARD_NUMBER = "LIB-000001";

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	private ObjectMapper mapper = new ObjectMapper();
	
	private LoanUpdatedV1NotificationHandler handler;
	
	@BeforeEach
	void setUp() {
		handler = new LoanUpdatedV1NotificationHandler(mapper, messagingTemplate);
	}
	
	@Test
	void shouldSendNotification() {

		NotificationEventEnvelope<LoanUpdatedNotificationPayload> event = buildLoanUpdatedEvent();

		handler.handleEvent(event);

		ArgumentCaptor<EventNotification> eventCaptor = ArgumentCaptor.forClass(EventNotification.class);

		verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/loans"), eventCaptor.capture());
		verify(messagingTemplate, times(1)).convertAndSendToUser(eq(event.payload().identityProviderId()), eq("/queue/loans"), eventCaptor.capture());
		verifyNoMoreInteractions(messagingTemplate);
		
		EventNotification sentEvent = eventCaptor.getValue();

		Assertions.assertEquals(NotificationEventType.LOAN_UPDATED, sentEvent.eventType());

		LoanUpdatedNotificationPayload payload = (LoanUpdatedNotificationPayload) sentEvent.payload();

		LoanUpdatedNotificationPayload expectedPayload = event.payload();

		Assertions.assertAll(() -> Assertions.assertEquals(expectedPayload.loanId(), payload.loanId()),
				() -> Assertions.assertEquals(expectedPayload.isbn(), payload.isbn()),
				() -> Assertions.assertEquals(expectedPayload.userId(), payload.userId()),
				() -> Assertions.assertEquals(expectedPayload.identityProviderId(), payload.identityProviderId()),
				() -> Assertions.assertEquals(expectedPayload.cardNumber(), payload.cardNumber()),
				() -> Assertions.assertEquals(expectedPayload.status(), payload.status()),
				() -> Assertions.assertEquals(expectedPayload.startDate(), payload.startDate()),
				() -> Assertions.assertEquals(expectedPayload.endDate(), payload.endDate()));
	}

	@Test
	void shouldRejectNullPayload() {

		NotificationEventEnvelope<?> event = new NotificationEventEnvelope<>(UUID.randomUUID().toString(),
				NotificationEventType.LOAN_UPDATED, "loan-service", Instant.now(), 1, null);

		NotificationHandlingException ex  = Assertions.assertThrows(NotificationHandlingException.class, () -> handler.handleEvent(event));
		Assertions.assertEquals("Payload is null", ex.getMessage());
		
		verifyNoInteractions(messagingTemplate);
	}

	private NotificationEventEnvelope<LoanUpdatedNotificationPayload> buildLoanUpdatedEvent() {
		return new NotificationEventEnvelope<LoanUpdatedNotificationPayload>(UUID.randomUUID().toString(), 
				NotificationEventType.LOAN_UPDATED, "loan-service", Instant.now(), 1, getPayload());
	}

	private LoanUpdatedNotificationPayload getPayload() {
		LocalDate start = LocalDate.now();
		LocalDate end = start.plusDays(30);
		return new LoanUpdatedNotificationPayload(UUID.randomUUID().toString(), "9788804336327", UUID.randomUUID().toString(), 
				UUID.randomUUID().toString(), CARD_NUMBER, LoanStatus.PENDING, start, end);
	}

}
