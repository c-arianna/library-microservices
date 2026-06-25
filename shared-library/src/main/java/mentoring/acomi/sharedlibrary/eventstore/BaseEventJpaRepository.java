package mentoring.acomi.sharedlibrary.eventstore;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseEventJpaRepository<ENTITY extends BaseEventEntity> extends JpaRepository<ENTITY, Long> {
    List<ENTITY> findByAggregateIdAndEventCategory(String aggregateId, String eventCategory);

    boolean existsByAggregateIdAndAggregateType(String aggregateId, String aggregateType);

    Optional<ENTITY> getByEventTypeAndAggregateId(String eventType, String aggregateId);
    
	boolean existsByEventIdAndAggregateTypeAndEventCategory(String eventId, String aggregateType, String eventCategory);

	Optional<Integer> findMaxProcessedVersion(String aggregateId, String aggregateType); 
	
	Optional<ENTITY> findNextEventToProcess(String aggregateId, String aggregateType, int eventVersion);
	
	boolean existsByEventIdAndAggregateTypeAndProcessedTrue(String eventId, String aggregateType);

	void markProcessed(String eventId, String aggregateType);

	List<ENTITY> findAllOrderByAggregateAndVersion();
	
}
