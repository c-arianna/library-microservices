package mentoring.acomi.userservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.sharedlibrary.eventstore.BaseEventJpaRepository;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

@Repository
public interface UserEventJpaRepository extends BaseEventJpaRepository<UserEventEntity> {

	@Query("""
			    select e
			    from UserEventEntity e
			    where e.aggregateId = :aggregateId
			    order by e.eventVersion asc
			""")
	List<UserEventEntity> findEventsForAggregate(@Param("aggregateId") String aggregateId);

	boolean existsByAggregateId(String aggregateId);

	Optional<UserEventEntity> getByEventTypeAndAggregateId(String eventType, String aggregateId);

	@Query("""
			    SELECT MAX(e.eventVersion)
			    FROM UserEventEntity e
			    WHERE e.aggregateId = :aggregateId AND e.aggregateType = :aggregateType AND e.processed = true
			""")
	Optional<Integer> findMaxProcessedVersion(@Param("aggregateId") String aggregateId, @Param("aggregateType") String aggregateType);
	
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			    UPDATE UserEventEntity e
			    SET e.processed = true
			    WHERE e.aggregateType = :aggregateType AND e.eventId = :eventId
			""")
	void markProcessed(@Param("eventId") String eventId, @Param("aggregateType") String aggregateType);
	
	@Query("""
		    select e
			    from UserEventEntity e
			    where e.aggregateId = :aggregateId and e.aggregateType = :aggregateType and e.eventVersion = :eventVersion and e.processed = false
		""")
	Optional<UserEventEntity> findNextEventToProcess(@Param("aggregateId") String aggregateId,  @Param("aggregateType") String aggregateType, 
			 @Param("eventVersion") int eventVersion);
}
