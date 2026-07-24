package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;

@Repository
public interface UserViewJpaRepository extends JpaRepository<UserViewEntity, String>{
	@Modifying
	@Query("UPDATE UserViewEntity u SET u.status = :status, u.updatedAt = :updateAt WHERE u.id = :id")
	int updateStatus(@Param("id") String id, @Param("status") UserStatus status, @Param("updateAt") Instant updateAt);
	@Query("""
			SELECT u 
			   FROM UserViewEntity u 
			      WHERE u.email = :email and u.status <> mentoring.acomi.sharedcorelibrary.model.UserStatus.DISABLED
		    """)
	Optional<UserViewEntity> findNotDisabledUserByEmail(String email);
	@Modifying
	@Transactional
	@Query("UPDATE UserViewEntity u SET u.cardNumber = :cardNumber, u.updatedAt = :updatedAt WHERE u.id = :id")
	void updateCardNumber(String id, String cardNumber, Instant updatedAt);
}
