package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import mentoring.acomi.loanservice.domain.events.LoanEventType;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.AbstractReplayService;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.ReplayEventHandlerRegistry;

@Service
public class LoanReplayService extends AbstractReplayService<LoanEventEntity> {

    private final LoanEventReplayRepository loanEventRepository;
    private final LoanReplayEventMapper replayMapper;

    public LoanReplayService(LoanEventReplayRepository loanEventRepository, LoanReplayEventMapper replayMapper, ReplayEventHandlerRegistry registry, 
    		List<ReplayProjection> replayProjections) {
        super(registry, replayProjections);
        this.loanEventRepository = loanEventRepository;
        this.replayMapper = replayMapper;
    }

    @Override
    protected Stream<LoanEventEntity> streamEvents() {
        return loanEventRepository.findAllEvents().stream();
    }
    
    @Override
    protected IntegrationEventEnvelope<?> toIntegrationEvent(LoanEventEntity entity) {

        if (EventCategory.CONSUMER.name().equalsIgnoreCase(entity.getEventCategory())) {

            return new IntegrationEventEnvelope<>(entity.getEventId(), IntegrationEventTypes.valueOf(entity.getEventType()), "",
                    			entity.getAggregateId(), entity.getAggregateType(), entity.getEventVersion(), entity.getOccurredAt(),
                    			entity.getSchemaVersion(), entity.getPayload());
        }

        LoanEventType loanEventType = LoanEventType.valueOf(entity.getEventType());

        Object payload = replayMapper.toIntegrationPayload(loanEventType, entity.getPayload());

        IntegrationEventTypes integrationType = replayMapper.toIntegrationEventType(loanEventType);

        return new IntegrationEventEnvelope<>(entity.getEventId(), integrationType, "", entity.getAggregateId(), entity.getAggregateType(),
                				entity.getEventVersion(), entity.getOccurredAt(), entity.getSchemaVersion(), payload);
    }
}