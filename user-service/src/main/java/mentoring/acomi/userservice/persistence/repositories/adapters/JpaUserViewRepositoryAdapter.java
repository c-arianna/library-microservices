package mentoring.acomi.userservice.persistence.repositories.adapters;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.application.UserFilter;
import mentoring.acomi.userservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.userservice.infrastructure.persistence.mapper.UserViewJpaMapper;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserViewJpaRepository;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.spec.JpaUserViewSpecification;

@Primary
@Repository
public class JpaUserViewRepositoryAdapter implements UserViewRepository, UserViewQueryRepository {

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
	public Optional<UserView> findByEmail(String email) {
		Optional<UserViewEntity> entity = repository.findByEmail(email);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public void updateStatus(String id, UserStatus status, Instant updatedAt) {
		repository.updateStatus(id, status, updatedAt);
	}

	@Override
	public void deleteAllReaderUsers() {
		repository.deleteAllReaderUsers();		
	}

	@Override
	public List<UserView> find(UserFilter filter) {
		Specification<UserViewEntity> spec = JpaUserViewSpecification.fromFilter(filter);
		return repository.findAll(spec).stream().map(mapper::toDomain).toList();
	}

}
