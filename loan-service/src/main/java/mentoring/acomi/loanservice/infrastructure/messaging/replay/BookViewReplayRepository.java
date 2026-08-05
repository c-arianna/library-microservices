package mentoring.acomi.loanservice.infrastructure.messaging.replay;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import mentoring.acomi.loanservice.application.repositories.BookViewRepository;
import mentoring.acomi.loanservice.application.view.BookView;
import mentoring.acomi.sharedcodelibrary.eventstore.replay.ReplayProjection;
import mentoring.acomi.sharedjpalibrary.eventstore.replay.ReplayTableManager;

@Repository("replayBookViewRepo")
public class BookViewReplayRepository implements BookViewRepository, ReplayProjection{

	private static final String TABLE_MAIN = "book_view";
	private static final String TABLE_TMP = "book_view_tmp";
	
	private final JdbcTemplate jdbcTemplate;
	private final ReplayTableManager replayTableManager;
		
	public BookViewReplayRepository(JdbcTemplate jdbcTemplate, ReplayTableManager replayTableManager) {
		this.jdbcTemplate = jdbcTemplate;
		this.replayTableManager = replayTableManager;
	}

	@Override
	public void insert(BookView view) {
		jdbcTemplate.update("INSERT INTO %s(isbn, author, title) VALUES (?, ?, ?)".formatted(TABLE_TMP), view.isbn(),
				view.author(), view.title());
		
	}

	@Override
	public Optional<BookView> findByIsbn(String isbn) {
		String query = "SELECT isbn, author, title FROM %s WHERE isbn = ?".formatted(TABLE_TMP);
		
		 List<BookView> results = jdbcTemplate.query(query,
				 (rs, rowNum) -> new BookView(rs.getString("isbn"), rs.getString("author"), rs.getString("title")), isbn);
		
		 return results.stream().findFirst();
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
}
