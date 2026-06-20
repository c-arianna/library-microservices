package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

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

}
