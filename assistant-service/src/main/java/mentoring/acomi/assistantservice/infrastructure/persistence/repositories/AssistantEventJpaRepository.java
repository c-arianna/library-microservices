package mentoring.acomi.assistantservice.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mentoring.acomi.assistantservice.infrastructure.persistence.entity.AssistantEventEntity;
import mentoring.acomi.sharedjpalibrary.eventstore.BaseEventJpaRepository;

public interface AssistantEventJpaRepository extends BaseEventJpaRepository<AssistantEventEntity> {

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			    UPDATE AssistantEventEntity e
			    SET e.processed = true
			    WHERE e.aggregateType = :aggregateType AND e.eventId = :eventId
			""")
	void markProcessed(@Param("eventId") String eventId, @Param("aggregateType") String aggregateType);
	
}
