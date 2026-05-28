package mentoring.acomi.bookservice.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mentoring.acomi.bookservice.infrastructure.persistence.entity.BookViewEntity;

@Repository
public interface BookViewJpaRepository
		extends JpaRepository<BookViewEntity, String>, JpaSpecificationExecutor<BookViewEntity> {

	@Modifying
	@Query("""
				UPDATE BookViewEntity b
				SET b.totalCopies = b.totalCopies + :quantity,
				    b.updatedAt = CURRENT_TIMESTAMP
				WHERE b.isbn = :isbn
			""")
	void addCopies(@Param("isbn") String isbn, @Param("quantity") int quantity);

	@Modifying
	@Query("""
			    UPDATE BookViewEntity b
			    SET b.totalCopies = b.totalCopies - :quantity,
			        b.updatedAt = CURRENT_TIMESTAMP
			    WHERE b.isbn = :isbn
			""")
	void removeCopies(@Param("isbn") String isbn, @Param("quantity") int quantity);
	
	@Modifying
	@Query("""
				UPDATE BookViewEntity b
				SET b.reservedCopies = b.reservedCopies + 1,
				    b.updatedAt = CURRENT_TIMESTAMP
				WHERE b.isbn = :isbn
			""")
	void reserve(@Param("isbn") String isbn);

	@Modifying
	@Query("""
			
			    UPDATE BookViewEntity b
			 	SET b.borrowedCopies = b.borrowedCopies + 1,
			     	b.reservedCopies = 
					    CASE 
					        WHEN b.reservedCopies > 0 
					        	THEN b.reservedCopies - 1 
					        	ELSE b.reservedCopies 
					     	END,
			     	b.updatedAt = CURRENT_TIMESTAMP
			 		WHERE b.isbn = :isbn
			""")
	void borrow(@Param("isbn")String isbn);

	@Modifying
	@Query("""
			    UPDATE BookViewEntity b
			 	SET b.reservedCopies = b.reservedCopies - 1,
			 	    b.updatedAt = CURRENT_TIMESTAMP
			 	    WHERE b.isbn = :isbn AND b.reservedCopies > 0
			""")
	void release(@Param("isbn")String isbn);

	@Modifying
	@Query("""
			    UPDATE BookViewEntity b
                SET b.borrowedCopies = b.borrowedCopies - 1,
			     	b.updatedAt = CURRENT_TIMESTAMP
			 		WHERE b.isbn = :isbn AND b.borrowedCopies > 0
			""")
	void returnBorrowed(@Param("isbn")String isbn);
	
}
