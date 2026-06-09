package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.bookservice.application.repositories.BookEventRepository;
import mentoring.acomi.bookservice.domain.events.BookEvent;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookEventEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookEventJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookEventJpaRepository;
import mentoring.acomi.sharedlibrary.eventstore.AbstractJpaEventRepositoryAdapter;

@Repository
@Transactional
public class JpaBookEventRepositoryAdapter extends AbstractJpaEventRepositoryAdapter<BookEvent, BookEventEntity> implements BookEventRepository{

	public JpaBookEventRepositoryAdapter(BookEventJpaRepository repository, BookEventJpaMapper mapper) {
		 super(repository, mapper);
	}

}
