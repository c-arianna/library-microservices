package mentoring.acomi.notificationservice.infrastructure.messaging.publisher;

import java.time.Instant;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.notificationservice.infrastructure.messaging.dto.BookSubscriptionNotifiedPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@Component
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final Tracer tracer;
	
	private static final Logger logger = LogManager.getLogger(NotificationEventPublisher.class);
	

    public NotificationEventPublisher(RabbitTemplate rabbitTemplate, Tracer tracer) {
        this.rabbitTemplate = rabbitTemplate;
        this.tracer = tracer;
    }

    public void publishBookSubscriptionNotified(long subscriptionId, String isbn) {

    	Span span = tracer.currentSpan();

    	NotificationEventEnvelope<BookSubscriptionNotifiedPayload> event =
                   new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.BOOK_SUBSCRIPTION_NOTIFIED,
                           "notification-service", Instant.now(), 1, new BookSubscriptionNotifiedPayload(subscriptionId, isbn));
    	       	
		logger.info("Publishing event {} traceId={} spanId={}", event.eventType().getRoutingKey(),
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");

		rabbitTemplate.convertAndSend(MessagingTopology.NOTIFICATIONS_EXCHANGE, event.eventType().getRoutingKey(), event);

    }
}