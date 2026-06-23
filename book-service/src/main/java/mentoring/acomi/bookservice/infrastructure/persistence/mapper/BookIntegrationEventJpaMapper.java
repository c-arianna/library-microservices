package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.EventMapper;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookIntegrationEventJpaMapper implements EventMapper<IntegrationEventEnvelope<?>, BookEventEntity>{

	private final ObjectMapper objectMapper;
	
	public BookIntegrationEventJpaMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Override
	public BookEventEntity toEntity(IntegrationEventEnvelope<?> event) {
		return new BookEventEntity(event.aggregateId(), event.aggregateType(), event.eventType().name(), event.eventId(), event.eventVersion(), event.schemaVersion(), toJsonNode(event.payload()), event.occurredAt());
	}

	@Override
	public IntegrationEventEnvelope<?> toDomain(BookEventEntity entity) {
		throw new UnsupportedOperationException("Not needed");
	}

	private JsonNode toJsonNode(Object payload) {
		return objectMapper.valueToTree(payload);
	}
}
