package mentoring.acomi.userservice.persistence.repositories.adapters;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import mentoring.acomi.userservice.application.repositories.UserViewRepository;
import mentoring.acomi.userservice.application.view.UserView;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.userservice.infrastructure.persistence.mapper.UserViewJpaMapper;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.UserViewJpaRepository;

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
	public Optional<UserView> findByEmail(String email) {
		Optional<UserViewEntity> entity = repository.findByEmail(email);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

}
