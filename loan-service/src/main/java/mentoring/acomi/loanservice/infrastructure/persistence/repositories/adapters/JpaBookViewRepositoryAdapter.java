package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.BookViewEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.BookViewJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.BookViewJpaRepository;

@Primary
@Repository
public class JpaBookViewRepositoryAdapter implements BookViewRepository {

	private final BookViewJpaRepository repository;
	private final BookViewJpaMapper mapper;
		
	public JpaBookViewRepositoryAdapter(BookViewJpaRepository repository, BookViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void insert(BookView view) {
		BookViewEntity entity = mapper.toEntity(view);
		repository.save(entity);
	}

	@Override
	public Optional<BookView> findByIsbn(String isbn) {
		Optional<BookViewEntity> entity = repository.findById(isbn);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(entity.get()));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();		
	}

}
