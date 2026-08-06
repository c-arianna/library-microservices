package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.time.Instant;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.repositories.BookRequestViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookRequestViewJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookRequestViewJpaRepository;

@Primary
@Repository
public class JpaBookRequestViewRepositoryAdapter implements BookRequestViewRepository {
	
	private final BookRequestViewJpaRepository repository;
	private final BookRequestViewJpaMapper mapper;
	
	public JpaBookRequestViewRepositoryAdapter(BookRequestViewJpaRepository repository, BookRequestViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void add(BookRequestView view) {
		BookRequestViewEntity entity = mapper.toEntity(view);
		repository.save(entity);
	}

	@Override
	public void registerVotes(String requestId, int votes, Instant updatedAt) {
		repository.registerVotes(requestId, votes, updatedAt);		
	}

	@Override
	public void updateStatus(String requestId, BookRequestStatus status, Instant updatedAt) {
		repository.updateStatus(requestId, status.name(), updatedAt);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}
	
}
