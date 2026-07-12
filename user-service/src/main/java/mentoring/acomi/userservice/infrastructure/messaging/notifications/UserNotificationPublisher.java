package mentoring.acomi.userservice.infrastructure.messaging.notifications;

import java.time.Instant;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.mapper.UserNotificationMapper;
import mentoring.acomi.userservice.infrastructure.messaging.notifications.payload.UserUpdatedNotificationPayload;

@Component
public class UserNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final UserNotificationMapper mapper;
    private final Tracer tracer;
    
    private static final Logger logger = LogManager.getLogger(UserNotificationPublisher.class);
    
    public UserNotificationPublisher(RabbitTemplate rabbitTemplate, UserNotificationMapper mapper, Tracer tracer) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.tracer = tracer;
    }

    public void publishUserUpdated(UserView user, int schemaVersion) {

    	Span span = tracer.currentSpan();

		logger.info("Publishing event {} traceId={} spanId={}", NotificationEventType.USER_UPDATED.getRoutingKey(),
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");
		
        NotificationEventEnvelope<UserUpdatedNotificationPayload> event =
                new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.USER_UPDATED,
                        "user-service", Instant.now(), schemaVersion, mapper.map(user));

        rabbitTemplate.convertAndSend(MessagingTopology.NOTIFICATIONS_EXCHANGE, NotificationEventType.USER_UPDATED.getRoutingKey(), event);

    }

}