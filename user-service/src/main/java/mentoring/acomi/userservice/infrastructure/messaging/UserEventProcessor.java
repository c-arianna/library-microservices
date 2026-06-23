package mentoring.acomi.userservice.infrastructure.messaging;

import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedlibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.userservice.application.errors.NonRetryableEventException;
import mentoring.acomi.userservice.application.projection.UserProjection;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.AggregateType;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserIntegrationPayload;
import mentoring.acomi.userservice.infrastructure.messaging.payload.producer.UserSubscribedIntegrationPayload;
import tools.jackson.databind.ObjectMapper;

@Component
public class UserEventProcessor {
	
	private final UserEventRepository userEventRepository;
	private final ObjectMapper mapper;
	private final UserProjection projection;
	private final UserIntegrationEventMapper eventMapper;
	
	public static final Map<IntegrationEventTypes, Integer> consumerSupportedVersion = 
			Map.ofEntries(Map.entry(IntegrationEventTypes.USER_SUBSCRIBED, UserIntegrationConsumerEventVersions.USER_SUBSCRIBED), 
					      Map.entry(IntegrationEventTypes.USER_UNSUBSCRIBED, UserIntegrationConsumerEventVersions.USER_UNSUBSCRIBED), 
					      Map.entry(IntegrationEventTypes.USER_SUSPENDED, UserIntegrationConsumerEventVersions.USER_SUSPENDED), 
					      Map.entry(IntegrationEventTypes.USER_UNSUSPENDED, UserIntegrationConsumerEventVersions.USER_UNSUSPENDED));
	
	private final Logger logger = LogManager.getLogger(UserEventProcessor.class);
	
	public UserEventProcessor(UserEventRepository userEventRepository, ObjectMapper mapper, UserProjection projection, UserIntegrationEventMapper eventMapper) {
		this.userEventRepository = userEventRepository;
		this.mapper = mapper;
		this.projection = projection;
		this.eventMapper = eventMapper;
		
	}
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processProducerEvent( IntegrationEventEnvelope<?> eventEnvelope) {
		
		if (userEventRepository.existsEventProcessed(eventEnvelope.eventId(), AggregateType.USER.name())) {
			return;
		}
		
		String aggregateId = eventEnvelope.aggregateId();
		String aggregateType = eventEnvelope.aggregateType();
		
		int lastEventVersionProcessed = userEventRepository.findMaxProcessedVersion(aggregateId, aggregateType).orElse(-1);
		int eventVersion = eventEnvelope.eventVersion();
		
		if(eventVersion == lastEventVersionProcessed + 1) {
			
			handleDomainEvent(eventEnvelope);
			userEventRepository.markProcessed(eventEnvelope.eventId(), aggregateType);
								
			int nextEventVersionToProcess = eventVersion + 1;
			
			while(true) {
				
				Optional<UserEvent> nextEventToProcess = userEventRepository.findNextEventToProcess(aggregateId, aggregateType, nextEventVersionToProcess);
				
				if(nextEventToProcess.isEmpty()) {
					break;
				}
				
				UserEvent eventToProcess = nextEventToProcess.get();
				handleDomainEvent(eventMapper.map(eventToProcess));
				userEventRepository.markProcessed(eventToProcess.eventId(), eventToProcess.aggregateType());
				
				nextEventVersionToProcess++;
			}
		}
		
	}
	
    private void handleDomainEvent(IntegrationEventEnvelope<?> eventEnvelope) {
		
		logger.info("Processing event {}, ID: {}", eventEnvelope.eventType().eventName, eventEnvelope.eventId());
		
		try {
			switch (eventEnvelope.eventType()) {
			
			case USER_SUBSCRIBED -> {
				UserSubscribedIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserSubscribedIntegrationPayload.class);
				projection.subscribeUser(payload);
			}
			
			case USER_UNSUBSCRIBED -> {
				UserIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserIntegrationPayload.class);
				projection.unsubscribeUser(payload);
			}
			
			case USER_SUSPENDED -> {
				UserIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserIntegrationPayload.class);
				projection.suspendUser(payload);
			}
			
			case USER_UNSUSPENDED -> {
				UserIntegrationPayload payload =  mapper.convertValue(eventEnvelope.payload(), UserIntegrationPayload.class);
				projection.unsuspendUser(payload);
			}
			
			default -> throw new NonRetryableEventException(String.format("Unknown event type: %s", eventEnvelope.eventType()));
				}
			logger.info("Event processed successfully");
		} catch (Exception e) {
			logger.error("Failed to process event {}", eventEnvelope.eventType(), e);
			throw e;
		}
	}

}
