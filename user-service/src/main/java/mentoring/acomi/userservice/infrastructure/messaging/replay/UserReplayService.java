package mentoring.acomi.userservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import mentoring.acomi.sharedcodelibrary.event.handlers.EventPayloadMapper;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.AbstractReplayService;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEventType;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

@Service
public class UserReplayService extends AbstractReplayService<UserEventEntity>{

	private final UserEventRepository userEventRepository;
	private final UserViewReplayRepository userViewReplayRepository;
	private final UserProjectionReplay projection;
	private final UserReplayEventMapper replayMapper;
	private final EventPayloadMapper payloadMapper;

	private final Map<UserEventType, Consumer<IntegrationEventEnvelope<?>>> handlers;
	
	private final Logger logger = LogManager.getLogger(UserReplayService.class);

	public UserReplayService(UserEventRepository userEventRepository, UserViewReplayRepository userViewReplayRepository, 
			UserProjectionReplay projection, UserReplayEventMapper replayMapper, EventPayloadMapper payloadMapper) {
		this.userEventRepository = userEventRepository;
		this.userViewReplayRepository = userViewReplayRepository;
		this.projection = projection;
		this.replayMapper = replayMapper;
		this.payloadMapper = payloadMapper;
		handlers =  Map.ofEntries(
				Map.entry(UserEventType.UserSubscribed, this::handleUserSubscribed),
				Map.entry(UserEventType.UserUnsubscribed, this::handleUserUnsubscribed),
				Map.entry(UserEventType.UserSuspended, this::handleUserSuspended),
				Map.entry(UserEventType.UserUnsuspended, this::handleUserUnsuspended));
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
	protected void apply(UserEventEntity event) {
		applyToTempTable(event);		
	}

	@Override
	protected void swapTables() {
		userViewReplayRepository.swapTables();		
	}

	@Override
	protected void dropTempTable() {
		userViewReplayRepository.dropTempTable();		
	}
	
	private void applyToTempTable(UserEventEntity entity) {

		if (entity.getSchemaVersion() != 1) {
		    throw new IllegalStateException("Unsupported schema version %s".formatted(entity.getSchemaVersion()));
		}

		UserEventType eventType = UserEventType.valueOf(entity.getEventType());
		IntegrationEventEnvelope<?> eventEnvelope  = getIntegrationEnvelopeEvent(entity, eventType);
		replayEvent(eventEnvelope, eventType);
	}

	private IntegrationEventEnvelope<?> getIntegrationEnvelopeEvent(UserEventEntity event, UserEventType userEventType) {
		Object payload = replayMapper.toIntegrationPayload(userEventType, event.getPayload());
		IntegrationEventTypes eventType = replayMapper.toIntegrationEventType(userEventType);
		return new IntegrationEventEnvelope<>(event.getEventId(), eventType, "",
				event.getAggregateId(), event.getAggregateType(), event.getEventVersion(), event.getOccurredAt(),
				event.getSchemaVersion(), payload);
	}
	
	private void replayEvent(IntegrationEventEnvelope<?> eventEnvelope, UserEventType eventType) {

		String eventId = eventEnvelope.eventId();

		Consumer<IntegrationEventEnvelope<?>> consumer = handlers.get(eventType);

		if (consumer == null) {
			logger.warn("No handler found for event {} ({})", eventId, eventType);
			return;
		}

		consumer.accept(eventEnvelope);

	}
	
	private void handleUserSubscribed(IntegrationEventEnvelope<?> event) {
		UserSubscribedIntegrationPayload payload = payloadMapper.mapAndValidate(event.payload(), UserSubscribedIntegrationPayload.class);
		projection.subscribeUser(payload, event.occurredAt());
	}

	private void handleUserUnsubscribed(IntegrationEventEnvelope<?> event) {
		UserIntegrationPayload payload = loadUserIntegrationPayload(event);
		projection.unsubscribeUser(payload, event.occurredAt());
	}

	private void handleUserSuspended(IntegrationEventEnvelope<?> event) {
		UserIntegrationPayload payload = loadUserIntegrationPayload(event);
		projection.suspendUser(payload, event.occurredAt());
	}

	private void handleUserUnsuspended(IntegrationEventEnvelope<?> event) {
		UserIntegrationPayload payload = loadUserIntegrationPayload(event);
		projection.unsuspendUser(payload, event.occurredAt());
	}

	private UserIntegrationPayload loadUserIntegrationPayload(IntegrationEventEnvelope<?> event) {
		return payloadMapper.mapAndValidate(event.payload(), UserIntegrationPayload.class);
	}
		
}
