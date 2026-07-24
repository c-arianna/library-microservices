package mentoring.acomi.userservice.infrastructure.persistence.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import mentoring.acomi.sharedcorelibrary.model.UserRole;
import mentoring.acomi.sharedcorelibrary.model.UserStatus;
import mentoring.acomi.userservice.infrastructure.persistence.entity.UserViewEntity;

@Repository
public interface UserViewJpaRepository extends JpaRepository<UserViewEntity, String>, JpaSpecificationExecutor<UserViewEntity>{
	@Query("""
			SELECT u 
			   FROM UserViewEntity u 
			      WHERE u.email = :email and u.status <> mentoring.acomi.sharedcorelibrary.model.UserStatus.DISABLED
		    """)
	Optional<UserViewEntity> findNotDisabledUserByEmail(String email);	
	@Modifying
	@Query("UPDATE UserViewEntity u SET u.status = :status, u.updatedAt = :updatedAt WHERE u.id = :id")
	int updateStatus(@Param("id") String id, @Param("status") UserStatus status, @Param("updatedAt") Instant updatedAt);
	@Modifying
	@Transactional
	@Query("DELETE from UserViewEntity u where u.role = 'READER'")
	void deleteAllReaderUsers();
	@Modifying
	@Transactional
	@Query("UPDATE UserViewEntity u SET u.cardNumber = :cardNumber, u.updatedAt = :updatedAt WHERE u.id = :id")
	void updateCardNumber(String id, String cardNumber, Instant updatedAt);
	@Query("SELECT u FROM UserViewEntity u WHERE u.cardNumber is null and u.role = :role")
	List<UserViewEntity> findWithoutCardNumber(@Param("role") UserRole role);
	@Modifying
	@Transactional
	void deleteByUserIdentityProviderId(String userIdentityProviderId);
	@Modifying
	@Query("""
	    UPDATE UserViewEntity u
	       SET u.status = mentoring.acomi.sharedcorelibrary.model.UserStatus.DISABLED,
	           u.activeEmail = null,
	           u.updatedAt = :updatedAt
	     WHERE u.id = :id
	""")
	void unsubscribeUser(@Param("id") String id,  @Param("updatedAt") Instant updatedAt);	
}
