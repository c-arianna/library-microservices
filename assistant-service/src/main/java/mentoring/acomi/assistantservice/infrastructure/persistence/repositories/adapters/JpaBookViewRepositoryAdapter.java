package mentoring.acomi.assistantservice.infrastructure.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import mentoring.acomi.assistantservice.application.repositories.BookViewRepository;
import mentoring.acomi.assistantservice.application.view.BookView;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.BookViewEntity;
import mentoring.acomi.assistantservice.infrastructure.persistence.mapper.BookViewJpaMapper;
import mentoring.acomi.assistantservice.infrastructure.persistence.repositories.BookViewJpaRepository;

@Primary
@Repository
public class JpaBookViewRepositoryAdapter extends AbstractJpaProjectionRepositoryAdapter<BookView, BookViewEntity, String> 
        implements BookViewRepository{
	
	private final BookViewJpaRepository repository;
		
	public JpaBookViewRepositoryAdapter(BookViewJpaRepository repository, BookViewJpaMapper mapper) {
		super(repository, mapper);
		this.repository = repository;
	}

	@Override
	public Optional<BookView> findByIsbn(String isbn) {
		return repository.findByIsbn(isbn).map(mapper::toView);
	}

	@Override
	public List<BookView> findAvailableBooks() {
		return repository.findAvailableBooks().stream().map(mapper::toView).toList();
	}
	
	@Override
	public List<BookView> findMostPopularBooks(int limit){
		return repository.findMostPopularBooks(PageRequest.of(0, limit)).stream().map(mapper::toView).toList();
	}
	
}