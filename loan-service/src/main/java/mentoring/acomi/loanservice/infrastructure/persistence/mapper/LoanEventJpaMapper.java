package mentoring.acomi.loanservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.loanservice.domain.events.LoanCanceledEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanConfirmedEvent;
import mentoring.acomi.loanservice.domain.events.LoanEvent;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.domain.events.LoanFailedEvent;
import mentoring.acomi.loanservice.domain.events.LoanRequestedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReservedEvent;
import mentoring.acomi.loanservice.domain.events.LoanReturnedEvent;
import mentoring.acomi.loanservice.domain.events.payload.LoanFailedPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanPayload;
import mentoring.acomi.loanservice.domain.events.payload.LoanRequestPayload;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.EventMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoanEventJpaMapper implements EventMapper<LoanEvent, LoanEventEntity>{

	private final ObjectMapper objectMapper;

	public LoanEventJpaMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public LoanEvent toDomain(LoanEventEntity event) {

		LoanEventType eventType = LoanEventType.valueOf(event.getEventType());

		return getEvent(eventType, event);

	}

	private LoanEvent getEvent(LoanEventType eventType, LoanEventEntity event) {

		return switch (eventType) {
		
		case LoanRequested -> {
			LoanRequestPayload payload = objectMapper.treeToValue(event.getPayload(), LoanRequestPayload.class);
			yield new LoanRequestedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}
		
		case LoanFailed -> {
			LoanFailedPayload payload = objectMapper.treeToValue(event.getPayload(), LoanFailedPayload.class);
			yield new LoanFailedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}

		case LoanReserved -> {
			LoanPayload payload = objectMapper.treeToValue(event.getPayload(), LoanPayload.class);
			yield new LoanReservedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}
		
		case LoanConfirmed -> {
			LoanPayload payload = objectMapper.treeToValue(event.getPayload(), LoanPayload.class);
			yield new LoanConfirmedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(),  payload, event.getOccurredAt());
		}
		
		case LoanCanceled -> {
			LoanPayload payload = objectMapper.treeToValue(event.getPayload(), LoanPayload.class);
			yield new LoanCanceledEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}
		
		case LoanReturned -> {
			LoanPayload payload = objectMapper.treeToValue(event.getPayload(), LoanPayload.class);
			yield new LoanReturnedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}
		
		case LoanConfirmRequested -> {
			LoanPayload payload = objectMapper.treeToValue(event.getPayload(), LoanPayload.class);
			yield new LoanConfirmRequestedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}
		
		};

	}

	public LoanEventEntity toEntity(LoanEvent event) {
		return new LoanEventEntity(event.aggregateId(), event.aggregateType(), event.eventId(), event.type().name(), event.eventVersion(), null, toJsonNode(event.payload()), event.occurredAt());

	}

	private JsonNode toJsonNode(Object payload) {
		return objectMapper.valueToTree(payload);
	}

}
