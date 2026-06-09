package mentoring.acomi.userservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.sharedlibrary.eventstore.BaseEventJpaRepository;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;

@Repository
public interface UserEventJpaRepository extends BaseEventJpaRepository<UserEventEntity> {

	@Query("""
			select max(e.eventVersion)
			from UserEventEntity e
			where e.aggregateId = :aggregateId
			""")
	Optional<Integer> findLastVersion(@Param("aggregateId") String aggregateId);

	@Query("""
			    select e
			    from UserEventEntity e
			    where e.aggregateId = :aggregateId
			    order by e.eventVersion asc
			""")
	List<UserEventEntity> findEventsForAggregate(@Param("aggregateId") String aggregateId);

	boolean existsByAggregateId(String aggregateId);
	
	Optional<UserEventEntity> getByEventTypeAndAggregateId(String eventType, String aggregateId);

}
