package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.application.repositories.LoanEventRepository;
import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.AbstractReplayService;

@Service
public class LoanReplayService extends AbstractReplayService<LoanEventEntity> {

	private final LoanEventRepository loanEventRepository;
	private final LoanViewReplayRepository loanViewReplayRepository;
	private final UserViewReplayRepository userViewReplayRepository;
	private final LoanReplayEventMapper replayMapper;
	private final ReplayEventHandlerRegistry registry;

	public LoanReplayService(LoanEventRepository loanEventRepository, LoanViewReplayRepository loanViewReplayRepository,
			UserViewReplayRepository userViewReplayRepository, LoanReplayEventMapper replayMapper, ReplayEventHandlerRegistry registry) {
		this.loanEventRepository = loanEventRepository;
		this.loanViewReplayRepository = loanViewReplayRepository;
		this.userViewReplayRepository = userViewReplayRepository;
		this.replayMapper = replayMapper;
		this.registry = registry;
	}

	@Override
	protected void createTempTable() {
		loanViewReplayRepository.createTempTable();
		userViewReplayRepository.createTempTable();
	}

	@Override
	protected List<LoanEventEntity> loadEvents() {
		return loanEventRepository.findAllEvents();
	}

	@Override
	protected void swapTables() {
		loanViewReplayRepository.swapTables();
		userViewReplayRepository.swapTables();
	}

	@Override
	protected void dropTempTable() {
		loanViewReplayRepository.dropTempTable();
		userViewReplayRepository.dropTempTable();
	}

	@Override
	protected void apply(LoanEventEntity entity) {
		IntegrationEventEnvelope<?> event = toEventEnvelope(entity);
		registry.find(event).ifPresentOrElse(handler -> handler.handleEvent(event),
				() -> logger.info("Replay not needed for {}", event.eventType()));
	}
	
	private IntegrationEventEnvelope<?> toEventEnvelope(LoanEventEntity entity) {

	    if (entity.getEventCategory().equalsIgnoreCase(EventCategory.CONSUMER.name())) {
	    	IntegrationEventTypes eventType = IntegrationEventTypes.valueOf(entity.getEventType());
	    	return new IntegrationEventEnvelope<>(entity.getEventId(), eventType, "",
	    			entity.getAggregateId(), entity.getAggregateType(), entity.getEventVersion(), entity.getOccurredAt(),
	    			entity.getSchemaVersion(), entity.getPayload());
	    }

	    LoanEventType loanEventType = LoanEventType.valueOf(entity.getEventType());

	    return getIntegrationEnvelopeEvent(entity, loanEventType);
	}
	
	private IntegrationEventEnvelope<?> getIntegrationEnvelopeEvent(LoanEventEntity event, LoanEventType loanEventType) {
		Object payload = replayMapper.toIntegrationPayload(loanEventType, event.getPayload());
		IntegrationEventTypes eventType = replayMapper.toIntegrationEventType(loanEventType);
		return new IntegrationEventEnvelope<>(event.getEventId(), eventType, "", event.getAggregateId(),
				event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(), event.getSchemaVersion(),
				payload);
	}
}
