package mentoring.acomi.notificationservice.messaging.publisher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import io.micrometer.tracing.Tracer;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookSubscriptionNotifiedPayload;
import mentoring.acomi.notificationservice.infrastructure.messaging.publisher.NotificationEventPublisher;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@ExtendWith(MockitoExtension.class)
class NotificationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private Tracer tracer;

    private NotificationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new NotificationEventPublisher(rabbitTemplate, tracer);
    }

    @Test
    void shouldPublishBookSubscriptionNotifiedEvent() {

        publisher.publishBookSubscriptionNotified(1L, "9788804336327");

        ArgumentCaptor<NotificationEventEnvelope<?>> captor = ArgumentCaptor.forClass(NotificationEventEnvelope.class);

        verify(rabbitTemplate).convertAndSend(eq(MessagingTopology.NOTIFICATIONS_EXCHANGE), 
        		eq(NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED.getRoutingKey()), captor.capture());

        NotificationEventEnvelope<?> event = captor.getValue();

        Assertions.assertEquals(NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED, event.eventType());

        BookSubscriptionNotifiedPayload payload = (BookSubscriptionNotifiedPayload) event.payload();

        Assertions.assertEquals(1L, payload.subscriptionId());
        Assertions.assertEquals("9788804336327", payload.isbn());
    }
}