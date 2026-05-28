package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.LoanEventEntity;

@Repository
public interface LoanEventJpaRepository extends JpaRepository<LoanEventEntity, Long> {

	@Query("""
			select max(e.eventVersion)
			from LoanEventEntity e
			where e.aggregateId = :aggregateId
			""")
	Optional<Integer> findLastVersion(@Param("aggregateId") String aggregateId);

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
