package mentoring.acomi.userservice.application.repositories;

import java.util.Optional;

import mentoring.acomi.userservice.application.view.RefreshToken;

public interface RefreshTokenRepository {
    public void save(RefreshToken token);
    public Optional<RefreshToken> findByToken(String token);
    public void revoke(String token);
}
