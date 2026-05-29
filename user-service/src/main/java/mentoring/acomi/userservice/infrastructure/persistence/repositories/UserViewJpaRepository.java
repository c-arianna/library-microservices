package mentoring.acomi.userservice.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;

@Repository
public interface UserViewJpaRepository extends JpaRepository<UserViewEntity, String>{
	public Optional<UserViewEntity> findByEmail(String email);
}
