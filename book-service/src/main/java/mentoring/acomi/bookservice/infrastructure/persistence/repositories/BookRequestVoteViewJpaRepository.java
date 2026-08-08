package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mentoring.acomi.bookservice.application.dto.BookRequestVoteDto;
import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestVoteViewEntity;

public interface BookRequestVoteViewJpaRepository extends JpaRepository<BookRequestVoteViewEntity, Long> {

	@Query("""
			select new mentoring.acomi.bookservice.application.dto.BookRequestVoteDto(
			    v.requestId,
			    v.userId,
			    u.cardNumber
			)
			from BookRequestVoteViewEntity v
			left join UserViewEntity u
			    on u.id = v.userId
			where v.requestId = :requestId
			""")
	List<BookRequestVoteDto> findVotesByRequestId(@Param("requestId") String requestId);
}
