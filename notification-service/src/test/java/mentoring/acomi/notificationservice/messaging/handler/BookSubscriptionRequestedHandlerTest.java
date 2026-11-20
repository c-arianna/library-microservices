package mentoring.acomi.notificationservice.messaging.handler;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.any;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mentoring.acomi.notificationservice.application.sender.SmsSender;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookSubscriptionRequestedPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.handlers.BookSubscriptionRequestedV1Handler;
import mentoring.acomi.notificationservice.infrastructure.messaging.publisher.NotificationEventPublisher;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationHandler;

@ExtendWith(MockitoExtension.class)
public class BookSubscriptionRequestedHandlerTest extends AbstractNotificationHandlerTest {
	
	@Mock
	private NotificationEventPublisher publisher;

	@Mock
	private  SmsSender smsSender;

	private BookSubscriptionRequestedV1Handler handler;

	@BeforeEach
	void setUp() {
		handler = new BookSubscriptionRequestedV1Handler(mapper, publisher, smsSender);
	}
	
	@Override
	protected NotificationHandler handler() {
		return handler;
	}

	@Override
	protected NotificationEventEnvelope<?> validEvent() {
		return buildBookSubscriptionRequestedEvent();
	}

	@Test
	void shouldSendNotification() {

		NotificationEventEnvelope<BookSubscriptionRequestedPayload> event = buildBookSubscriptionRequestedEvent();

		handler.handleEvent(event);

		InOrder inOrder = inOrder(smsSender, publisher);
						
		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

		inOrder.verify(smsSender).send(eq(event.payload().phoneNumber()), messageCaptor.capture());

		Assertions.assertTrue(messageCaptor.getValue().contains(event.payload().isbn()));
		Assertions.assertTrue(messageCaptor.getValue().contains(event.payload().title()));
		
		inOrder.verify(publisher, times(1)).publishBookSubscriptionNotified(event.payload().subscriptionId(), event.payload().isbn());
	}
	
	@Test
	void shouldNotPublishNotificationEventWhenSmsSendingFails() {

	    NotificationEventEnvelope<BookSubscriptionRequestedPayload> event = buildBookSubscriptionRequestedEvent();

	    doThrow(new RuntimeException("SMS error")).when(smsSender).send(any(), any());

	    RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> handler.handleEvent(event));

	    Assertions.assertEquals("SMS error", ex.getMessage());

	    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
	    
	    verify(smsSender, times(1)).send(eq(event.payload().phoneNumber()), messageCaptor.capture());

	    verifyNoInteractions(publisher);
	}

	private NotificationEventEnvelope<BookSubscriptionRequestedPayload> buildBookSubscriptionRequestedEvent() {
		return new NotificationEventEnvelope<BookSubscriptionRequestedPayload>(UUID.randomUUID().toString(), 
				NotificationEventType.BOOK_SUBSCRIPTION_REQUESTED, "book-service", Instant.now(), 1, getPayload());
	}

	private BookSubscriptionRequestedPayload getPayload() {
		return new BookSubscriptionRequestedPayload(1L, "9788804336327", UUID.randomUUID().toString(), "+390000000000", "Il barone rampante");
	}

}
