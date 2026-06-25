package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.repositories.BookViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookViewJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookViewJpaRepository;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.spec.JpaBookViewSpecification;

@Primary
@Repository
public class JpaBookViewRepositoryAdapter implements BookViewRepository, BookViewQueryRepository {

	private final BookViewJpaRepository repository;
	private final BookViewJpaMapper mapper;

	public JpaBookViewRepositoryAdapter(BookViewJpaRepository repository, BookViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void addBook(BookView book, Instant createdAt) {
		BookViewEntity entity = mapper.toEntity(book);
		entity.markCreated(createdAt);
		repository.save(entity);
	}

	@Override
	public List<BookView> find(BookFilter filter) {
		Specification<BookViewEntity> spec = JpaBookViewSpecification.fromFilter(filter);
		return repository.findAll(spec).stream().map(mapper::toView).toList();
	}

	@Override
	public void updateCopies(String isbn, int quantity, Instant updatedAt) {
		repository.updateCopies(isbn, quantity, updatedAt);		
	}

	@Override
	public Optional<BookView> findById(String isbn) {
		Optional<BookViewEntity> entity = repository.findById(isbn);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(entity.get()));
	}

	@Override
	public void reserve(String isbn, Instant updatedAt) {
		repository.reserve(isbn, updatedAt);		
	}

	@Override
	public void borrow(String isbn, Instant updatedAt) {
		repository.borrow(isbn, updatedAt);
	}

	@Override
	public void release(String isbn, Instant updatedAt) {
		repository.release(isbn, updatedAt);
	}

	@Override
	public void returnBorrowed(String isbn, Instant updatedAt) {
		repository.returnBorrowed(isbn, updatedAt);		
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();		
	}

}
