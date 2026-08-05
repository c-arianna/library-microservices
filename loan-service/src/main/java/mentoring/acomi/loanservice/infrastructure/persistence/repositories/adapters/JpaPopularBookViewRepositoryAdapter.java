package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.PopularBookViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.PopularBookViewRepository;
import mentoring.acomi.loanservice.application.view.PopularBookView;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.PopularBookViewEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.PopularBookViewJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.PopularBookViewJpaRepository;

@Primary
@Repository
public class JpaPopularBookViewRepositoryAdapter implements PopularBookViewRepository, PopularBookViewQueryRepository {

	private final PopularBookViewJpaRepository repository;
	private final PopularBookViewJpaMapper mapper;		
	
	public JpaPopularBookViewRepositoryAdapter(PopularBookViewJpaRepository repository, PopularBookViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void registerLoanCount(PopularBookView view) {
		repository.registerLoanCount(view.isbn(), view.author(), view.title(), view.loanCount());
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();		
	}

	@Override
	public Optional<PopularBookView> findByIsbn(String isbn) {
		Optional<PopularBookViewEntity> entity = repository.findById(isbn);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toView(entity.get()));
	}

	@Override
	public List<PopularBookView> findMostPopularBooks(int limit) {
		return repository.findMostPopularBooks(PageRequest.of(0, limit)).stream().map(mapper::toView).toList();
	}

}
