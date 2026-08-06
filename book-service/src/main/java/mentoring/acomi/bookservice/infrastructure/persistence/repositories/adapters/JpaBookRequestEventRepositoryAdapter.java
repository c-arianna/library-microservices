package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.repositories.BookRequestEventRepository;
import mentoring.acomi.bookservice.domain.events.bookrequest.BookRequestEvent;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookRequestEventJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookEventJpaRepository;
import mentoring.acomi.sharedjpalibrary.eventstore.AbstractJpaEventRepositoryAdapter;

@Repository
@Transactional
public class JpaBookRequestEventRepositoryAdapter extends AbstractJpaEventRepositoryAdapter<BookRequestEvent, BookEventEntity> implements BookRequestEventRepository {

	public JpaBookRequestEventRepositoryAdapter(BookEventJpaRepository repository, BookRequestEventJpaMapper mapper) {
		 super(repository, mapper);
	}
	
	@Override
	public List<BookEventEntity> findAllEvents(){
		return repository.findAllByOrderByOccurredAtAscIdAsc();
	}

}
