package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.repositories.BookRequestViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.BookRequestViewRepository;
import mentoring.acomi.bookservice.application.view.BookRequestView;
import mentoring.acomi.bookservice.domain.bookrequest.model.BookRequestStatus;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookRequestViewJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookRequestViewJpaRepository;

@Primary
@Repository
public class JpaBookRequestViewRepositoryAdapter implements BookRequestViewRepository, BookRequestViewQueryRepository {
	
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
		repository.updateStatus(requestId, status, updatedAt);
	}

	@Override
	public void updatePrice(String requestId, BigDecimal estimatedPrice, Instant updatedAt) {
		repository.updatePrice(requestId, estimatedPrice, updatedAt);	
	}
	
	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public Optional<BookRequestView> findPendingRequestByIsbn(String isbn) {
		Optional<BookRequestViewEntity> bookRequest = repository.findPendingRequestByIsbn(isbn);
		return bookRequest.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(bookRequest.get()));
	}

	@Override
	public Optional<BookRequestView> findPendingRequestByAuthorAndTitle(String author, String title) {
		Optional<BookRequestViewEntity> bookRequest = repository.findPendingRequestByAuthorAndTitle(author, title);
		return bookRequest.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(bookRequest.get()));
	}

	@Override
	public List<BookRequestView> findBookRequests() {
		return repository.findAll().stream().map(mapper::toView).toList();
	}

	@Override
	public Optional<BookRequestView> findById(String requestId) {
		Optional<BookRequestViewEntity> bookRequest = repository.findById(requestId);
		return bookRequest.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(bookRequest.get()));
	}
	
}
