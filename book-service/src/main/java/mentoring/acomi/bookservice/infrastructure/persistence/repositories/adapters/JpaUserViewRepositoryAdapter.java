package mentoring.acomi.bookservice.infrastructure.persistence.repositories.adapters;

import java.time.Instant;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.application.repositories.UserViewQueryRepository;
import mentoring.acomi.bookservice.application.repositories.UserViewRepository;
import mentoring.acomi.bookservice.application.view.UserView;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.bookservice.infrastructure.persistence.mapper.UserViewJpaMapper;
import mentoring.acomi.bookservice.infrastructure.persistence.repositories.UserViewJpaRepository;

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
	public Optional<UserView> findById(String id) {
		Optional<UserViewEntity> entity = repository.findById(id);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public Optional<UserView> findNotDisabledUserByEmail(String email) {
		Optional<UserViewEntity> entity = repository.findNotDisabledUserByEmail(email);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public void add(UserView user, Instant createdAt) {
		UserViewEntity entity = mapper.toEntity(user);
		entity.markCreated(createdAt);
		repository.save(entity);
	}
	
	@Override
	public void unsubscribeUser(String userId, Instant occurredAt) {
		repository.unsubscribeUser(userId, occurredAt);		
	}
	
	@Override
	public void deleteAll() {
		repository.deleteAll();
	}	

}
