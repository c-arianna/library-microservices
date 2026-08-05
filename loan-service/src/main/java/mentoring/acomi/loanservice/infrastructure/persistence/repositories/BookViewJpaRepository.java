package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.BookViewEntity;

public interface BookViewJpaRepository extends JpaRepository<BookViewEntity, String>{

}
