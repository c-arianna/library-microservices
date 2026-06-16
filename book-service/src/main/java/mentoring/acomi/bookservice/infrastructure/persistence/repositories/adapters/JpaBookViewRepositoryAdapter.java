package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.BookFilter;
import mentoring.acomi.bookservice.application.repositories.BookViewRepository;
import mentoring.acomi.bookservice.application.view.BookView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.BookViewJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.BookViewJpaRepository;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.spec.JpaBookViewSpecification;

@Repository
public class JpaBookViewRepositoryAdapter implements BookViewRepository {

	private final BookViewJpaRepository repository;
	private final BookViewJpaMapper mapper;

	public JpaBookViewRepositoryAdapter(BookViewJpaRepository repository, BookViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void addBook(BookView book) {
		BookViewEntity entity = mapper.toEntity(book);
		repository.save(entity);
	}

	@Override
	public List<BookView> find(BookFilter filter) {
		Specification<BookViewEntity> spec = JpaBookViewSpecification.fromFilter(filter);
		return repository.findAll(spec).stream().map(mapper::toView).toList();
	}

	@Override
	public void addCopies(String isbn, int quantity) {
		repository.addCopies(isbn, quantity);		
	}

	@Override
	public void removeCopies(String isbn, int quantity) {
		repository.removeCopies(isbn, quantity);		
	}

	@Override
	public Optional<BookView> findById(String isbn) {
		Optional<BookViewEntity> entity = repository.findById(isbn);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(entity.get()));
	}

	@Override
	public void reserve(String isbn) {
		repository.reserve(isbn);		
	}

	@Override
	public void borrow(String isbn) {
		repository.borrow(isbn);
	}

	@Override
	public void release(String isbn) {
		repository.release(isbn);
	}

	@Override
	public void returnBorrowed(String isbn) {
		repository.returnBorrowed(isbn);		
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();		
	}

}
