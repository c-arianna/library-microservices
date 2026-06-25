package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.BaseEventJpaRepository;

@Repository
public interface LoanEventJpaRepository extends BaseEventJpaRepository<LoanEventEntity> {

	@Query("""
			    select e
			    from LoanEventEntity e
			    where e.aggregateId = :aggregateId
			    order by e.eventVersion asc
			""")
	List<LoanEventEntity> findEventsForAggregate(@Param("aggregateId") String aggregateId);

	boolean existsByAggregateId(String aggregateId);

	Optional<LoanEventEntity> getByEventTypeAndAggregateId(String eventType, String aggregateId);

	@Query("""
			    SELECT MAX(e.eventVersion)
			    FROM LoanEventEntity e
			    WHERE e.aggregateId = :aggregateId AND e.aggregateType = :aggregateType AND e.processed = true
			""")
	Optional<Integer> findMaxProcessedVersion(@Param("aggregateId") String aggregateId, @Param("aggregateType") String aggregateType);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			    UPDATE LoanEventEntity e
			    SET e.processed = true
			    WHERE e.aggregateType = :aggregateType AND e.eventId = :eventId
			""")
	void markProcessed(@Param("eventId") String eventId, @Param("aggregateType") String aggregateType);
	
	@Query("""
		    select e
			    from LoanEventEntity e
			    where e.aggregateId = :aggregateId and e.aggregateType = :aggregateType and e.eventVersion = :eventVersion and e.processed = false
		""")
	Optional<LoanEventEntity> findNextEventToProcess(@Param("aggregateId") String aggregateId,  @Param("aggregateType") String aggregateType, 
			@Param("eventVersion") int eventVersion);
	
	@Query("""
		    SELECT e
		    FROM LoanEventEntity e
		    ORDER BY e.aggregateType, e.aggregateId, e.eventVersion
		""")
     List<LoanEventEntity> findAllOrderByAggregateAndVersion();

}
