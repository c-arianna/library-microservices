package mentoring.acomi.userservice.infrastructure.messaging.replay;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.AbstractReplayService;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

@Service
public class UserReplayService extends AbstractReplayService<UserEventEntity>{

	private final UserEventRepository userEventRepository;
	private final UserViewReplayRepository userViewReplayRepository;
	private final UserReplayEventMapper replayMapper;
	private final ReplayEventHandlerRegistry registry;

	private final Logger logger = LogManager.getLogger(UserReplayService.class);

	public UserReplayService(UserEventRepository userEventRepository, UserViewReplayRepository userViewReplayRepository, 
			UserReplayEventMapper replayMapper, ReplayEventHandlerRegistry registry) {
		this.userEventRepository = userEventRepository;
		this.userViewReplayRepository = userViewReplayRepository;
		this.replayMapper = replayMapper;
		this.registry = registry;
   }

	@Override
	protected void createTempTable() {
		userViewReplayRepository.createTempTable();		
	}

	@Override
	protected List<UserEventEntity> loadEvents() {
		return userEventRepository.findAllEvents();
	}

	@Override
	protected void apply(UserEventEntity entity) {
		UserEventType eventType = UserEventType.valueOf(entity.getEventType());
		IntegrationEventEnvelope<?> eventEnvelope  = getIntegrationEnvelopeEvent(entity, eventType);	
		registry.find(eventEnvelope).ifPresentOrElse(handler -> handler.handleEvent(eventEnvelope),
				() -> logger.info("Replay not needed for {}", eventType));
	}

	@Override
	protected void swapTables() {
		userViewReplayRepository.swapTables();		
	}

	@Override
	protected void dropTempTable() {
		userViewReplayRepository.dropTempTable();		
	}
	
	private IntegrationEventEnvelope<?> getIntegrationEnvelopeEvent(UserEventEntity event, UserEventType userEventType) {
		Object payload = replayMapper.toIntegrationPayload(userEventType, event.getPayload());
		IntegrationEventTypes eventType = replayMapper.toIntegrationEventType(userEventType);
		return new IntegrationEventEnvelope<>(event.getEventId(), eventType, "",
				event.getAggregateId(), event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(),
				event.getSchemaVersion(), payload);
	}
		
}
