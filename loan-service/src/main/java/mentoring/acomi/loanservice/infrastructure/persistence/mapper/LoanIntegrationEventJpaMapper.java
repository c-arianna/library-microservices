package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoanIntegrationEventJpaMapper implements EventMapper<IntegrationEventEnvelope<?>, LoanEventEntity>{

	private final ObjectMapper objectMapper;
	
	public LoanIntegrationEventJpaMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
	@Override
	public LoanEventEntity toEntity(IntegrationEventEnvelope<?> event) {
		return new LoanEventEntity(event.aggregateId(), event.aggregateType(), event.eventId(),  event.eventType().name(), event.eventVersion(), event.schemaVersion(), toJsonNode(event.payload()), event.occurredAt());
	}

	@Override
	public IntegrationEventEnvelope<?> toDomain(LoanEventEntity entity) {
		throw new UnsupportedOperationException("Not needed");
	}

	private JsonNode toJsonNode(Object payload) {
		return objectMapper.valueToTree(payload);
	}
}
