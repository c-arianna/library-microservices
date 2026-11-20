package mentoring.acomi.assistantservice.infrastructure.persistence.repositories.adapters;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.assistantservice.application.repositories.UserViewRepository;
import mentoring.acomi.assistantservice.application.view.UserView;
import mentoring.acomi.assistantservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.assistantservice.infrastructure.persistence.mapper.UserViewJpaMapper;
import mentoring.acomi.assistantservice.infrastructure.persistence.repositories.UserViewJpaRepository;

@Primary
@Repository
public class JpaUserViewRepositoryAdapter extends AbstractJpaProjectionRepositoryAdapter<UserView, UserViewEntity, String> 
                implements UserViewRepository{

	private final UserViewJpaRepository repository;
		
	public JpaUserViewRepositoryAdapter(UserViewJpaRepository repository, UserViewJpaMapper mapper) {
		super(repository, mapper);
		this.repository = repository;
	}

	@Override
	public Optional<UserView> findByUserIdentityProviderId(String identityProviderId) {
		return repository.findByUserIdentityProviderId(identityProviderId).map(mapper::toView);
	}
}
