package mentoring.acomi.userservice.persistence.repositories.adapters;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.sharedjpalibrary.eventstore.AbstractJpaEventRepositoryAdapter;
import mentoring.acomi.userservice.application.repositories.UserEventRepository;
import mentoring.acomi.userservice.domain.events.UserEvent;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserEventEntity;
import mentoring.acomi.userservice.infrastructure.persistence.mapper.UserEventJpaMapper;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserEventJpaRepository;

@Repository
@Transactional
public class JpaUserEventRepositoryAdapter extends AbstractJpaEventRepositoryAdapter<UserEvent, UserEventEntity> implements UserEventRepository {

	public JpaUserEventRepositoryAdapter(UserEventJpaRepository repository, UserEventJpaMapper mapper) {
		 super(repository, mapper);
	}
	
	@Override
	public List<UserEventEntity> findAllEvents(){
		return repository.findAllByOrderByOccurredAtAscIdAsc();
	}

}
