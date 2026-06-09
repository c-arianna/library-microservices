package mentoring.acomi.sharedlibrary.eventstore;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseEventJpaRepository<ENTITY extends BaseEventEntity> extends JpaRepository<ENTITY, Long> {

    Optional<Integer> findLastVersion(String aggregateId);

    List<ENTITY> findEventsForAggregate(String aggregateId);

    boolean existsByAggregateId(String aggregateId);

    Optional<ENTITY> getByEventTypeAndAggregateId(String eventType, String aggregateId);
}
