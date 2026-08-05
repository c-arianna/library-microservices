package mentoring.acomi.loanservice.infrastructure.persistence.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import mentoring.acomi.loanservice.infrastructure.persistence.entity.PopularBookViewEntity;

public interface PopularBookViewJpaRepository extends JpaRepository<PopularBookViewEntity, String>{

	@Modifying
	@Transactional
	@NativeQuery("""
			 INSERT INTO popular_book_view (isbn, author, title, loan_count) VALUES (:isbn, :author, :title, :loanCount)
            ON DUPLICATE KEY UPDATE
                loan_count = loan_count + 1
			""")
	void registerLoanCount(@Param("isbn") String isbn, @Param("author") String author,  @Param("title") String title, 
		@Param("loanCount") int loanCount);
	
	@Query("""
		    select b
		    from PopularBookViewEntity b
		    order by b.loanCount desc
		    """)
	List<PopularBookViewEntity> findMostPopularBooks(Pageable pageable);
}
