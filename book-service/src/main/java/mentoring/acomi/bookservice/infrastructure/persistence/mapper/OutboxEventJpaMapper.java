package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.OutboxEventEntity;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;

@Component
public class OutboxEventJpaMapper {
	
	public OutboxEventEntity toEntity(OutboxEvent event) {
		return new OutboxEventEntity(event.eventId(), event.aggregateType(), event.status(), event.retryCount(),
				event.lastError(),  event.createdAt(),event.publishedAt(), event.nextRetryAt());
	}
	
	public OutboxEvent toDomain(OutboxEventEntity entity) {
		return new OutboxEvent(entity.getEventId(), entity.getAggregateType(), entity.getStatus(), entity.getRetryCount(),
				entity.getLastError(), entity.getCreatedAt(), entity.getPublishedAt(), 
				entity.getNextRetryAt());
	}

}

