package mentoring.acomi.userservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import mentoring.acomi.sharedcodelibrary.eventstore.replay.AbstractReplayService;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.ReplayEventHandlerRegistry;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

@Service
public class UserReplayService extends AbstractReplayService<UserEventEntity> {

	private final UserEventRepository userEventRepository;
	private final UserReplayEventMapper replayMapper;

	public UserReplayService(UserEventRepository userEventRepository, UserReplayEventMapper replayMapper, 
			ReplayEventHandlerRegistry registry, List<ReplayProjection> replayProjections) {
		super(registry, replayProjections);
		this.userEventRepository = userEventRepository;
		this.replayMapper = replayMapper;
	}
	
	@Override
	protected Stream<UserEventEntity> streamEvents() {
		return userEventRepository.findAllEvents().stream();
	}

	@Override
	protected IntegrationEventEnvelope<?> toIntegrationEvent(UserEventEntity entity) {

		UserEventType eventType = UserEventType.valueOf(entity.getEventType());

		Object payload = replayMapper.toIntegrationPayload(eventType, entity.getPayload());

		IntegrationEventTypes integrationType = replayMapper.toIntegrationEventType(eventType);

		return new IntegrationEventEnvelope<>(entity.getEventId(), integrationType, "", entity.getAggregateId(),
				entity.getAggregateType(), entity.getEventVersion(), entity.getOccurredAt(), entity.getSchemaVersion(),
				payload);
	}
}
