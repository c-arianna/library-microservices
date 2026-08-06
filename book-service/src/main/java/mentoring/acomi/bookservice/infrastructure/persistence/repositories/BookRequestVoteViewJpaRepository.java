package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookRequestVoteViewEntity;

public interface BookRequestVoteViewJpaRepository extends JpaRepository<BookRequestVoteViewEntity, Long> {

}
