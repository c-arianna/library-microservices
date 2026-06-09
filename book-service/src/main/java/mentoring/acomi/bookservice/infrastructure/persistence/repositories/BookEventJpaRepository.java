package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.sharedlibrary.eventstore.BaseEventJpaRepository;

@Repository
public interface BookEventJpaRepository extends BaseEventJpaRepository<BookEventEntity> {

	@Query("""
			select max(e.eventVersion)
			from BookEventEntity e
			where e.aggregateId = :aggregateId
			""")
	Optional<Integer> findLastVersion(@Param("aggregateId") String aggregateId);

	@Query("""
			    select e
			    from BookEventEntity e
			    where e.aggregateId = :aggregateId
			    order by e.eventVersion asc
			""")
	List<BookEventEntity> findEventsForAggregate(@Param("aggregateId") String aggregateId);

	boolean existsByAggregateId(String aggregateId);
	
	Optional<BookEventEntity> getByEventTypeAndAggregateId(String eventType, String aggregateId);

}
