package mentoring.acomi.userservice.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;

import mentoring.acomi.userservice.application.view.RefreshToken;
import mentoring.acomi.userservice.infrastructure.persistence.entity.RefreshTokenEntity;

@Component
public class RefreshTokenJpaMapper {

	public RefreshTokenEntity toEntity(RefreshToken token) {
		return new RefreshTokenEntity(token.userId(), token.token(), token.expiresAt(), token.revoked());
	}
	
	public RefreshToken toDomain(RefreshTokenEntity entity) {
		return new RefreshToken(entity.getUserId(), entity.getToken(), entity.getExpiresAt(), entity.isRevoked());
	}
}
