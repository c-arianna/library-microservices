package mentoring.acomi.bookservice.infrastructure.messaging.notifications;

import java.time.Instant;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.bookservice.application.view.BookSubscriptionView;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.mapper.BookNotificationMapper;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload.BookSubscriptionRequestedPayload;
import mentoring.acomi.bookservice.infrastructure.messaging.notifications.payload.BookUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@Component
public class BookNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final BookNotificationMapper mapper;
    private final Tracer tracer;
    
    private static final Logger logger = LogManager.getLogger(BookNotificationPublisher.class);
    
    public BookNotificationPublisher(RabbitTemplate rabbitTemplate, BookNotificationMapper mapper, Tracer tracer) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.tracer = tracer;
    }

    public void publishBookUpdated(BookView book) {

    	Span span = tracer.currentSpan();

		logger.info("Publishing event {} traceId={} spanId={}", NotificationEventType.BOOK_UPDATED.getRoutingKey(),
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");
		
        NotificationEventEnvelope<BookUpdatedNotificationPayload> event =
                new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.BOOK_UPDATED,
                        "book-service", Instant.now(), 1, mapper.map(book));

        rabbitTemplate.convertAndSend(MessagingTopology.NOTIFICATIONS_EXCHANGE, NotificationEventType.BOOK_UPDATED.getRoutingKey(), event);

    }

	public void publishBookSubscriptionRequested(BookSubscriptionView subscription, String title) {
	
		Span span = tracer.currentSpan();

		logger.info("Publishing event {} traceId={} spanId={}", NotificationEventType.BOOK_SUBSCRIPTION_REQUESTED.getRoutingKey(),
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");
		
		NotificationEventEnvelope<?> event = getBookSubscriptionRequested(subscription, title);
		
		rabbitTemplate.convertAndSend(MessagingTopology.NOTIFICATIONS_EXCHANGE, NotificationEventType.BOOK_SUBSCRIPTION_REQUESTED.getRoutingKey(), event);
		
	}

	private NotificationEventEnvelope<?> getBookSubscriptionRequested(BookSubscriptionView subscription, String title) {
		
		BookSubscriptionRequestedPayload payload = new BookSubscriptionRequestedPayload(subscription.id(), subscription.isbn(),
				subscription.userIdentityId(), subscription.phoneNumber(), title);
		
		return new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.BOOK_SUBSCRIPTION_REQUESTED, "book-service", 
				Instant.now(), 1, payload);
	}

}