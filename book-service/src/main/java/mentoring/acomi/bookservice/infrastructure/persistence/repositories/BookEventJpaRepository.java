package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedjpalibrary.eventstore.BaseEventJpaRepository;

@Repository
public interface BookEventJpaRepository extends BaseEventJpaRepository<BookEventEntity> {

	@Query("""
			    select e
			    from BookEventEntity e
			    where e.aggregateId = :aggregateId and e.aggregateType = :aggregateType
			    order by e.eventVersion asc
			""")
	List<BookEventEntity> findEventsForAggregate(@Param("aggregateId") String aggregateId, @Param("aggregateType") String aggregateType);

	boolean existsByAggregateId(String aggregateId);

	Optional<BookEventEntity> getByEventTypeAndAggregateId(String eventType, String aggregateId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			    UPDATE BookEventEntity e
			    SET e.processed = true
			    WHERE e.aggregateType = :aggregateType AND e.eventId = :eventId
			""")
	void markProcessed(@Param("eventId") String eventId, @Param("aggregateType") String aggregateType);

}
