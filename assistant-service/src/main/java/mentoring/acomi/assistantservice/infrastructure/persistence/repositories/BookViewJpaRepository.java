package mentoring.acomi.assistantservice.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import mentoring.acomi.assistantservice.infrastructure.persistence.entity.BookViewEntity;

public interface BookViewJpaRepository extends JpaRepository<BookViewEntity, String> {

	Optional<BookViewEntity> findByIsbn(String isbn);
	
	@Query("""
			SELECT e
			FROM BookViewEntity e
			WHERE e.totalCopies - e.borrowedCopies - e.reservedCopies > 0
		""")
	List<BookViewEntity> findAvailableBooks();
	
	@Query("""
		    SELECT b
		    FROM BookViewEntity b
		    WHERE b.isbn IN (
		        SELECT r.isbn
		        FROM ReaderHistoryViewEntity r
		        WHERE r.loanStatus IN (
		            mentoring.acomi.sharedcorelibrary.model.LoanStatus.CONFIRMED,
		            mentoring.acomi.sharedcorelibrary.model.LoanStatus.RETURNED
		        )
		        GROUP BY r.isbn
		        ORDER BY COUNT(r) DESC
		    )
		""")
	List<BookViewEntity> findMostPopularBooks(Pageable pageable);
}
