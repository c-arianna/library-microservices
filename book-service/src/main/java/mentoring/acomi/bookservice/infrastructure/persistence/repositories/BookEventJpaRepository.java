package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.BaseEventJpaRepository;

@Repository
public interface BookEventJpaRepository extends BaseEventJpaRepository<BookEventEntity> {

	@Query("""
			    select e
			    from BookEventEntity e
			    where e.aggregateId = :aggregateId
			    order by e.eventVersion asc
			""")
	List<BookEventEntity> findEventsForAggregate(@Param("aggregateId") String aggregateId);

	boolean existsByAggregateId(String aggregateId);

	Optional<BookEventEntity> getByEventTypeAndAggregateId(String eventType, String aggregateId);

	@Query("""
			    SELECT MAX(e.eventVersion)
			    FROM BookEventEntity e
			    WHERE e.aggregateId = :aggregateId AND e.aggregateType = :aggregateType AND e.processed = true
			""")
	Optional<Integer> findMaxProcessedVersion(@Param("aggregateId") String aggregateId,
			@Param("aggregateType") String aggregateType);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			    UPDATE BookEventEntity e
			    SET e.processed = true
			    WHERE e.aggregateType = :aggregateType AND e.eventId = :eventId
			""")
	void markProcessed(@Param("eventId") String eventId, @Param("aggregateType") String aggregateType);

	@Query("""
			    select e
				    from BookEventEntity e
				    where e.aggregateId = :aggregateId and e.aggregateType = :aggregateType and e.eventVersion = :eventVersion and e.processed = false
			""")
	Optional<BookEventEntity> findNextEventToProcess(@Param("aggregateId") String aggregateId,
			@Param("aggregateType") String aggregateType, @Param("eventVersion") int eventVersion);

	@Query("""
			    SELECT e
			    FROM BookEventEntity e
			    ORDER BY e.aggregateType, e.aggregateId, e.eventVersion
			""")
	List<BookEventEntity> findAllOrderByAggregateAndVersion();
	
	@Query("""
		    UPDATE BookEventEntity e
		    SET e.failed = true
		    WHERE e.aggregateType = :aggregateType AND e.eventId = :eventId
		""")
    void markFailed(@Param("eventId") String eventId, @Param("aggregateType") String aggregateType);
	
	@Query("""
		    UPDATE BookEventEntity e
		    SET e.retryCount = e.retryCount +1
		    WHERE e.aggregateType = :aggregateType AND e.eventId = :eventId
		""")
	void incrementRetry(String eventId, String aggregateType);

}
