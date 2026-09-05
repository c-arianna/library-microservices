package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.PopularBookViewRepository;
import mentoring.acomi.loanservice.application.view.PopularBookView;
import mentoring.acomi.sharedcorelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Repository("replayPopularBookViewRepo")
public class PopularBookViewReplay implements PopularBookViewRepository, ReplayProjection{

	private static final String TABLE_MAIN = "popular_book_view";
	private static final String TABLE_TMP = "popular_book_view_tmp";
	
	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;
	
	public PopularBookViewReplay(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.replayTableManager = replayTableManager;
	}
	
	@Override
	public void registerLoanCount(PopularBookView view) {
		jdbcTemplate.update("""
				INSERT INTO %s(isbn, author, title, loan_count) VALUES (?, ?, ?, ?)
					ON DUPLICATE KEY UPDATE loan_count = loan_count + 1;
				""".formatted(TABLE_TMP), view.isbn(), view.author(), view.title(), view.loanCount());
	}
	
	@Override
	public void createTempTable() {
		replayTableManager.createTempTable(TABLE_MAIN, TABLE_TMP);
	}

	@Override
	public void swapTables() {
		replayTableManager.swapTables(TABLE_MAIN, TABLE_TMP);
	}

	@Override
	public void dropTempTable() {
		replayTableManager.dropTempTable(TABLE_TMP);		
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException("Not needed");		
	}

	@Override
	public Optional<PopularBookView> findByIsbn(String isbn) {
		String query = "SELECT isbn, author, title, loan_count FROM %s WHERE isbn = ?".formatted(TABLE_TMP);
		
		 List<PopularBookView> results = jdbcTemplate.query(query,
				 (rs, rowNum) -> new PopularBookView(rs.getString("isbn"), rs.getString("author"), rs.getString("title"), 
						 rs.getInt("loan_count")), isbn);
		
		 return results.stream().findFirst();
	}

}
