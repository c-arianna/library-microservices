package mentoring.acomi.assistantservice.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mentoring.acomi.assistantservice.infrastructure.persistence.entity.UserViewEntity;

public interface UserViewJpaRepository extends JpaRepository<UserViewEntity, String> {
	Optional<UserViewEntity> findByUserIdentityProviderId(String identityProviderId);
}
