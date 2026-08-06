package mentoring.acomi.bookservice.infrastructure.messaging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import mentoring.acomi.bookservice.domain.events.book.BookEvent;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.MessagingTopology;

@Component
public class BookIntegrationEventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final BookIntegrationEventMapper mapper;
	private final Tracer tracer;
	
	private static final Logger logger = LogManager.getLogger(BookIntegrationEventPublisher.class);

	public BookIntegrationEventPublisher(RabbitTemplate rabbitTemplate, BookIntegrationEventMapper mapper, Tracer tracer) {
		this.rabbitTemplate = rabbitTemplate;
		this.mapper = mapper;
		this.tracer = tracer;
	}

	private void publish(IntegrationEventEnvelope<?> eventEnvelope) {

		Span span = tracer.currentSpan();

		logger.info("Publishing event {} traceId={} spanId={}", eventEnvelope.eventType().getRoutingKey(),
				span != null ? span.context().traceId() : "null", span != null ? span.context().spanId() : "null");

		rabbitTemplate.convertAndSend(MessagingTopology.EVENTS_EXCHANGE, eventEnvelope.eventType().getRoutingKey(),
				eventEnvelope);
	}

	public void dispatch(BookEvent event) {
		IntegrationEventEnvelope<?> eventToPublish = mapper.map(event);
		publish(eventToPublish);
	}
}