package mentoring.acomi.sharedjpalibrary.eventstore;

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
		
	boolean existsByEventIdAndAggregateTypeAndProcessedTrue(String eventId, String aggregateType);

	void markProcessed(String eventId, String aggregateType);

	List<ENTITY> findAllOrderByAggregateAndVersion();
	
	 Optional<ENTITY> getByEventIdAndAggregateType(String eventId, String aggregateType);
		
}
