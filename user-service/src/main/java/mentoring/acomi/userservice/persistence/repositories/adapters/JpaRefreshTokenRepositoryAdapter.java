package mentoring.acomi.userservice.persistence.repositories.adapters;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import mentoring.acomi.userservice.application.repositories.RefreshTokenRepository;
import mentoring.acomi.userservice.application.view.RefreshToken;
import mentoring.acomi.userservice.infrastructure.persistence.entity.RefreshTokenEntity;
import mentoring.acomi.userservice.infrastructure.persistence.mapper.RefreshTokenJpaMapper;
import mentoring.acomi.userservice.infrastructure.persistence.repositories.RefreshTokenJpaRepository;

@Repository
public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepository {

	private final RefreshTokenJpaRepository repository;
	private final RefreshTokenJpaMapper mapper;

	public JpaRefreshTokenRepositoryAdapter(RefreshTokenJpaRepository repository, RefreshTokenJpaMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public void save(RefreshToken token) {
		RefreshTokenEntity entity =  mapper.toEntity(token);
		repository.save(entity);
	}

	@Override
	public Optional<RefreshToken> findByToken(String token) {
		Optional<RefreshTokenEntity> entity = repository.findByToken(token);
		return entity.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(entity.get()));
	}

	@Override
	public void revoke(String token) {
		repository.findByToken(token).ifPresent(entity -> {
			entity.revoke();
			repository.save(entity);
		});
	}
}
