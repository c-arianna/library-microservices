package mentoring.acomi.bookservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestAddedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestApprovedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEventType;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestPriceUpdatedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestRejectedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestVotedEvent;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestAddedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestApprovedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestPriceUpdatedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestRejectedPayload;
import mentoring.acomi.bookservice.domain.events.bookrequest.payload.BookRequestVotedPayload;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventMapper;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class BookRequestEventJpaMapper implements EventMapper<BookRequestEvent, BookEventEntity>{

	private final ObjectMapper objectMapper;

	public BookRequestEventJpaMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public BookRequestEvent toDomain(BookEventEntity event) {
		BookRequestEventType eventType = BookRequestEventType.valueOf(event.getEventType());
		return getEvent(eventType, event);
	}

	private BookRequestEvent getEvent(BookRequestEventType eventType, BookEventEntity event) {

		return switch (eventType) {

		case BookRequestAdded -> {
			BookRequestAddedPayload payload = objectMapper.treeToValue(event.getPayload(), BookRequestAddedPayload.class);
			yield new BookRequestAddedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}

		case BookRequestApproved -> {
			BookRequestApprovedPayload payload = objectMapper.treeToValue(event.getPayload(), BookRequestApprovedPayload.class);
			yield new BookRequestApprovedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}

		case BookRequestVoted -> {
			BookRequestVotedPayload payload = objectMapper.treeToValue(event.getPayload(), BookRequestVotedPayload.class);
			yield new BookRequestVotedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
		}

		case BookRequestRejected -> {
			BookRequestRejectedPayload payload = objectMapper.treeToValue(event.getPayload(), BookRequestRejectedPayload.class);
			yield new BookRequestRejectedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, event.getOccurredAt());
			
		}
		case BookRequestPriceUpdated -> {
			BookRequestPriceUpdatedPayload payload = objectMapper.treeToValue(event.getPayload(), BookRequestPriceUpdatedPayload.class);
			yield new BookRequestPriceUpdatedEvent(event.getAggregateId(), event.getEventId(), event.getEventVersion(), payload, 
					event.getOccurredAt());
		}	
				
		};

	}

	public BookEventEntity toEntity(BookRequestEvent event) {
		return new BookEventEntity(event.aggregateId(), event.aggregateType(), event.type().name(), event.eventId(), event.eventVersion(), null, toJsonNode(event.payload()), event.occurredAt());
	}

	private JsonNode toJsonNode(Object payload) {
		return objectMapper.valueToTree(payload);
	}

}
