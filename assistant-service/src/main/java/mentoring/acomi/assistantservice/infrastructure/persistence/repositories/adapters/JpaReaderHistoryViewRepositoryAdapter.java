package mentoring.acomi.assistantservice.infrastructure.persistence.repositories.adapters;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.assistantservice.application.repositories.ReaderHistoryViewRepository;
import mentoring.acomi.assistantservice.application.view.ReaderHistoryView;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.ReaderHistoryViewEntity;
import mentoring.acomi.assistantservice.infrastructure.persistence.mapper.ReaderHistoryViewMapper;
import mentoring.acomi.assistantservice.infrastructure.persistence.repositories.ReaderHistoryViewJpaRepository;

@Primary
@Repository
public class JpaReaderHistoryViewRepositoryAdapter extends AbstractJpaProjectionRepositoryAdapter<ReaderHistoryView, 
        ReaderHistoryViewEntity, String> implements ReaderHistoryViewRepository {
	
	private final ReaderHistoryViewJpaRepository repository;
		
	protected JpaReaderHistoryViewRepositoryAdapter(ReaderHistoryViewJpaRepository repository, ReaderHistoryViewMapper mapper) {
		super(repository, mapper);
		this.repository = repository;
	}

	@Override
	public List<ReaderHistoryView> findByUserId(String userId) {
		return repository.findByUserId(userId).stream().map(mapper::toView).toList();
	}

}
