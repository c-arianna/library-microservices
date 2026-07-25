package mentoring.acomi.userservice.persistence.repositories.adapters;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import mentoring.acomi.sharedcorelibrary.outbox.OutboxEvent;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxRepository;
import mentoring.acomi.sharedcorelibrary.outbox.OutboxStatus;
import mentoring.acomi.userservice.infrastructure.persistence.entity.OutboxEventEntity;
import mentoring.acomi.userservice.infrastructure.persistence.mapper.OutboxEventJpaMapper;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.OutboxJpaRepository;

@Repository
public class JpaOutboxRepositoryAdapter implements OutboxRepository {

	private final OutboxJpaRepository repository;
	private final OutboxEventJpaMapper mapper;
		
	public JpaOutboxRepositoryAdapter(OutboxJpaRepository repository, OutboxEventJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void add(OutboxEvent outboxEvent) {
		OutboxEventEntity entity = mapper.toEntity(outboxEvent);
		repository.save(entity);		
	}

	@Override
	public List<OutboxEvent> findEventsToPublish(Instant now, int limit) {
		return repository.findEventsToPublish(OutboxStatus.PENDING, now, PageRequest.of(0, limit)).stream()
				.map(mapper::toDomain).toList();
	}

	@Override
	public void published(String eventId, Instant publishedAt) {
		repository.published(eventId, publishedAt);
	}

	@Override
	public void recordFailure(String eventId, OutboxStatus status, String lastError, int retryCount, Instant nextRetryAt) {
		repository.recordFailure(eventId, status, lastError, retryCount, nextRetryAt);
		
	}

}
