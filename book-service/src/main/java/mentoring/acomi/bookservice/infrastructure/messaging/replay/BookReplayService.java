package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEventType;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.AbstractReplayService;

@Service
public class BookReplayService extends AbstractReplayService<BookEventEntity> {

	private final BookEventRepository bookEventRepository;
	private final BookViewReplayRepository bookViewReplayRepository;
	private final BookReplayEventMapper replayMapper;
	private final ReplayEventHandlerRegistry registry;
	
	public BookReplayService(BookViewReplayRepository bookViewReplayRepository, BookEventRepository bookEventRepository, 
			BookReplayEventMapper replayMapper, ReplayEventHandlerRegistry registry) {
		this.bookEventRepository = bookEventRepository;
		this.bookViewReplayRepository = bookViewReplayRepository;
		this.replayMapper = replayMapper;
		this.registry = registry;
	}

	@Override
	protected void createTempTable() {
		bookViewReplayRepository.createTempTable();
	}

	@Override
	protected List<BookEventEntity> loadEvents() {
		return bookEventRepository.findAllEvents();
	}
	
	@Override
	protected void apply(BookEventEntity entity) {
	    IntegrationEventEnvelope<?> event = toEventEnvelope(entity);
	    registry.find(event).ifPresentOrElse(handler -> handler.handleEvent(event), 
  		      () -> logger.info("Replay not needed for {}", event.eventType()));
	}
	
	private IntegrationEventEnvelope<?> toEventEnvelope(BookEventEntity entity) {

	    if (entity.getEventCategory().equalsIgnoreCase(EventCategory.CONSUMER.name())) {
	    	IntegrationEventTypes eventType = IntegrationEventTypes.valueOf(entity.getEventType());
	        return new IntegrationEventEnvelope<>(entity.getEventId(), eventType, "",
	        		entity.getAggregateId(), entity.getAggregateType(), entity.getEventVersion(), entity.getOccurredAt(),
	        		entity.getSchemaVersion(), entity.getPayload());
	    }

	    BookEventType bookEventType = BookEventType.valueOf(entity.getEventType());

	    return getIntegrationEnvelopeEvent(entity, bookEventType);
	}
	
	@Override
	protected void swapTables() {
		bookViewReplayRepository.swapTables();
	}

	@Override
	protected void dropTempTable() {
		bookViewReplayRepository.dropTempTable();
	}
			
	private IntegrationEventEnvelope<?> getIntegrationEnvelopeEvent(BookEventEntity event, BookEventType bookEventType) {
		Object payload = replayMapper.toIntegrationPayload(bookEventType, event.getPayload());
		IntegrationEventTypes eventType = replayMapper.toIntegrationEventType(bookEventType);
		return new IntegrationEventEnvelope<>(event.getEventId(), eventType, "",
				event.getAggregateId(), event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(),
				event.getSchemaVersion(), payload);
	}
	
}