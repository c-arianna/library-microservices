package mentoring.acomi.loanservice.infrastructure.persistence.repositories.adapters;

import java.time.Instant;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.loanservice.application.repositories.UserViewRepository;
import mentoring.acomi.loanservice.application.view.UserView;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.loanservice.infrastructure.persistence.mapper.UserViewJpaMapper;
import mentoring.acomi.loanservice.infrastructure.persistence.repositories.UserViewJpaRepository;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@Primary
@Repository
public class JpaUserViewRepositoryAdapter implements UserViewRepository, UserViewQueryRepository{

	private final UserViewJpaRepository repository;
	private final UserViewJpaMapper mapper;

	public JpaUserViewRepositoryAdapter(UserViewJpaRepository repository, UserViewJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void add(UserView user, Instant createdAt) {
		UserViewEntity entity = mapper.toEntity(user);
		entity.markCreated(createdAt);
		repository.save(entity);
	}

	@Override
	public Optional<UserView> findById(String id) {
		Optional<UserViewEntity> entity = repository.findById(id);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public void updateStatus(String id, UserStatus status, Instant updateAt) {
		repository.updateStatus(id, status, updateAt);
	}

	@Override
	public Optional<UserView> findByEmail(String email) {
		Optional<UserViewEntity> entity = repository.findByEmail(email);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();		
	}

}
