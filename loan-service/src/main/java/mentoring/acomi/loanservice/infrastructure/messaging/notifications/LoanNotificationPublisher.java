package mentoring.acomi.loanservice.infrastructure.messaging.notifications;

import java.time.Instant;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.application.view.LoanView;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.mapper.LoanNotificationMapper;
import mentoring.acomi.loanservice.infrastructure.messaging.notifications.payload.LoanUpdatedNotificationPayload;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.notifications.NotificationEventType;

@Component
public class LoanNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final LoanNotificationMapper mapper;
    private final Tracer tracer;
    
    private static final Logger logger = LogManager.getLogger(LoanNotificationPublisher.class);
    
    public LoanNotificationPublisher(RabbitTemplate rabbitTemplate, LoanNotificationMapper mapper, Tracer tracer) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.tracer = tracer;
    }

    public void publishLoanUpdated(LoanView loan, BookView book, String identityProviderId, String cardNumber) {

    	Span span = tracer.currentSpan();

		logger.info("Publishing event {} traceId={} spanId={}", NotificationEventType.LOAN_UPDATED.getRoutingKey(),
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");
		
        NotificationEventEnvelope<LoanUpdatedNotificationPayload> event =
                new NotificationEventEnvelope<>(UUID.randomUUID().toString(), NotificationEventType.LOAN_UPDATED,
                        "loan-service", Instant.now(), 1, mapper.map(loan, book, identityProviderId, cardNumber));

        rabbitTemplate.convertAndSend(MessagingTopology.NOTIFICATIONS_EXCHANGE, NotificationEventType.LOAN_UPDATED.getRoutingKey(), event);

    }

}