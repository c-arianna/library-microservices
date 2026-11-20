package mentoring.acomi.assistantservice.infrastructure.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mentoring.acomi.assistantservice.infrastructure.persistence.entity.ReaderHistoryViewEntity;

public interface ReaderHistoryViewJpaRepository extends JpaRepository<ReaderHistoryViewEntity, String>{
	List<ReaderHistoryViewEntity> findByUserId(String userId);
}
