package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.UserViewEntity;


public interface UserViewJpaRepository extends JpaRepository<UserViewEntity, String> {
	
	@Query("""
			SELECT u 
			   FROM UserViewEntity u 
			      WHERE u.email = :email and u.status <> mentoring.acomi.sharedcorelibrary.model.UserStatus.DISABLED
		    """)
	Optional<UserViewEntity> findNotDisabledUserByEmail(String email);
	
	@Modifying
	@Query("""
	    UPDATE UserViewEntity u
	       SET u.status = mentoring.acomi.sharedcorelibrary.model.UserStatus.DISABLED, u.updatedAt = :updatedAt
	     WHERE u.id = :id
	""")
	void unsubscribeUser(@Param("id") String id,  @Param("updatedAt") Instant updatedAt);	

}
