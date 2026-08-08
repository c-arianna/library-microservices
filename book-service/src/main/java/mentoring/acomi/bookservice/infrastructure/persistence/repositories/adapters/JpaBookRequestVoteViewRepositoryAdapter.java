package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.dto.BookRequestVoteDto;
import mentoring.acomi.bookservice.application.repositories.BookRequestVoteViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.BookRequestVoteViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestVoteView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestVoteViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookRequestVoteViewJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookRequestVoteViewJpaRepository;

@Primary
@Repository
public class JpaBookRequestVoteViewRepositoryAdapter implements BookRequestVoteViewRepository, BookRequestVoteViewQueryRepository {

	private final BookRequestVoteViewJpaRepository repository;
	private final BookRequestVoteViewJpaMapper mapper;
	
	public JpaBookRequestVoteViewRepositoryAdapter(BookRequestVoteViewJpaRepository repository, BookRequestVoteViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void add(BookRequestVoteView view) {
		BookRequestVoteViewEntity entity = mapper.toEntity(view);
		repository.save(entity);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public List<BookRequestVoteDto> findVotesByRequestId(String requestId) {
		return repository.findVotesByRequestId(requestId);
	}

}
