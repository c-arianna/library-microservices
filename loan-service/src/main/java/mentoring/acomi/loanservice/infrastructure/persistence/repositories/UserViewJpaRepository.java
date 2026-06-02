package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.UserViewEntity;
import mentoring.acomi.sharedlibrary.model.UserStatus;

@Repository
public interface UserViewJpaRepository extends JpaRepository<UserViewEntity, String>{
	@Modifying
	@Query("UPDATE UserViewEntity u SET u.status = :status, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
	int updateStatus(@Param("id") String id, @Param("status") UserStatus status);
}
