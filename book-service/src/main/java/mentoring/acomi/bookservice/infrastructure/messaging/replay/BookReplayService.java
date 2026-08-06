package mentoring.acomi.bookservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.AggregateType;
import mentoring.acomi.bookservice.domain.events.ProducerEventType;
import mentoring.acomi.bookservice.domain.events.book.BookEventType;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEventType;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.AbstractReplayService;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedcorelibrary.eventstore.EventCategory;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventEnvelope;
import mentoring.acomi.sharedcorelibrary.integration.messaging.IntegrationEventTypes;
import mentoring.acomi.sharedcorelibrary.replay.ReplayEventHandlerRegistry;

@Service
public class BookReplayService extends AbstractReplayService<BookEventEntity> {

    private final BookEventRepository bookEventRepository;
    private final BookReplayEventMapper replayMapper;

    public BookReplayService(BookEventRepository bookEventRepository, BookReplayEventMapper replayMapper, 
    		ReplayEventHandlerRegistry registry, List<ReplayProjection> replayProjections) {
        super(registry, replayProjections);
        this.bookEventRepository = bookEventRepository;
        this.replayMapper = replayMapper;
    }

    @Override
    protected Stream<BookEventEntity> streamEvents() {
        return bookEventRepository.findAllEvents().stream();
    }

    @Override
    protected IntegrationEventEnvelope<?> toIntegrationEvent(BookEventEntity entity) {

        if (EventCategory.CONSUMER.name().equalsIgnoreCase(entity.getEventCategory())) {

            return new IntegrationEventEnvelope<>(entity.getEventId(), IntegrationEventTypes.valueOf(entity.getEventType()), "", 
            		            entity.getAggregateId(), entity.getAggregateType(), entity.getEventVersion(), entity.getOccurredAt(),
            		            entity.getSchemaVersion(), entity.getPayload());
        }

        return getIntegrationProducerEvent(entity);
      
    }

    private IntegrationEventEnvelope<?> getIntegrationProducerEvent(BookEventEntity entity) {

        ProducerEventType producerEventType = getProducerEventType(entity);

        Object payload = replayMapper.toIntegrationPayload(producerEventType, entity.getPayload());

        IntegrationEventTypes integrationType = replayMapper.toIntegrationEventType(producerEventType);

        return new IntegrationEventEnvelope<>(entity.getEventId(), integrationType, "", entity.getAggregateId(),
                entity.getAggregateType(), entity.getEventVersion(), entity.getOccurredAt(), entity.getSchemaVersion(), payload);
    }
    
    private ProducerEventType getProducerEventType(BookEventEntity entity) {

        return switch (AggregateType.valueOf(entity.getAggregateType())) {

            case BOOK -> BookEventType.valueOf(entity.getEventType());

            case BOOK_REQUEST -> BookRequestEventType.valueOf(entity.getEventType());

            default -> throw new IllegalStateException();
        };
    }
}