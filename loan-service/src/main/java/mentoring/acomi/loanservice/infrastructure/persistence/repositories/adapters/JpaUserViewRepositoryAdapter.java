package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.UserViewJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.UserViewJpaRepository;
import mentoring.acomi.sharedlibrary.model.UserStatus;

@Repository
public class JpaUserViewRepositoryAdapter implements UserViewRepository {

	private final UserViewJpaRepository repository;
	private final UserViewJpaMapper mapper;

	public JpaUserViewRepositoryAdapter(UserViewJpaRepository repository, UserViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void add(UserView user) {
		UserViewEntity entity = mapper.toEntity(user);
		repository.save(entity);
	}

	@Override
	public Optional<UserView> findById(String id) {
		Optional<UserViewEntity> entity = repository.findById(id);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public void updateStatus(String id, UserStatus status) {
		repository.updateStatus(id, status);
	}

	@Override
	public Optional<UserView> findByEmail(String email) {
		Optional<UserViewEntity> entity = repository.findByEmail(email);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

}
